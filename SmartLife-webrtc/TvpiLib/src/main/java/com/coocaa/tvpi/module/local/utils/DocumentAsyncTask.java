package com.coocaa.tvpi.module.local.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.local.document.DocumentConfig;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coocaa.tvpi.module.local.document.DocumentConfig.ALL_SCAN_PATHS;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/21
 */
public class DocumentAsyncTask extends AsyncTask<Void, DocumentData, List<DocumentData>> {

    private final static String TAG = DocumentAsyncTask.class.getSimpleName();
    private Context mContext;
    private DocumentConfig.Source mCurSource;
    private DocumentBrowseCallback mDocumentBrowseCallback;
    private List<DocumentData> mProessFiles = new ArrayList<>();
    private Map<DocumentConfig.Source, List<DocumentData>> mFileCacheMap = new HashMap<>();

    public interface DocumentBrowseCallback {
        void onResult(List<DocumentData> result);

        void onProgress(List<DocumentData> datas);

        void onPathProgress(String currentScanPath);
    }

    public DocumentAsyncTask(Context context, String pathSourceText, DocumentBrowseCallback documentBrowseCallback) {
        mContext = context;
        mCurSource = DocumentConfig.getSourceByText(pathSourceText);
        mDocumentBrowseCallback = documentBrowseCallback;
    }

    @Override
    protected List<DocumentData> doInBackground(Void... params) {
        try {
            mFileCacheMap.clear();
            mFileCacheMap.put(DocumentConfig.Source.ALL, scanRootPathFile());
            List<String> scanPathList = SpUtil.getList(mContext, DocumentUtil.SP_KEY_SCAN_PATH);
            if (scanPathList == null) {
                scanPathList = new ArrayList<>();
            }
            scanPathList.addAll(Arrays.asList(ALL_SCAN_PATHS));
            for (String path : scanPathList) {
                if (isCancelled()) {
                    Log.i(TAG, "doInBackground: task is isCancelled, break.");
                    break;
                }
                DocumentConfig.Source cacheKey = DocumentConfig.getSourceByPath(path);
                List<DocumentData> dataList = scanLocalPathFiles(path);
                mFileCacheMap.get(DocumentConfig.Source.ALL).addAll(dataList);
                if (!DocumentConfig.Source.ALL.equals(cacheKey)) {
                    if (mFileCacheMap.containsKey(cacheKey)) {
                        mFileCacheMap.get(cacheKey).addAll(dataList);
                    } else {
                        mFileCacheMap.put(cacheKey, dataList);
                    }
                }
            }
            //提前排序
            for (List<DocumentData> dataList : mFileCacheMap.values()) {
                sortFiles(dataList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFileCacheMap.get(mCurSource);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i(TAG, "onCancelled: ");
    }

    @Override
    protected void onPostExecute(List<DocumentData> result) {
        if (null != mDocumentBrowseCallback)
            mDocumentBrowseCallback.onResult(result);
    }

    private List<DocumentData> filterFromFormat(List<DocumentData> sourceFiles, String format) {
        if (!FormatEnum.contains(format)) {
            return sourceFiles;
        }
        List<DocumentData> dataList = new ArrayList<>();
        for (DocumentData filterData : sourceFiles) {
            if (filterData.format.equals(format)) {
                dataList.add(filterData);
            }
        }
        return dataList;
    }

    public List<DocumentData> filterFiles(String source, String format) {
        return filterFromFormat(mFileCacheMap.get(DocumentConfig.getSourceByText(source)), format);
    }

    private List<DocumentData> scanRootPathFile() {
        Log.i(TAG, "scanRootPathFile: start.");
        List<DocumentData> dataList = new ArrayList<>();
        try {
            File rootPathFile = new File(DocumentUtil.PATH_EXTERNAL);
            File[] files = rootPathFile.listFiles();
            for (File f : files) {
                if (isCancelled()) {
                    Log.i(TAG, "scanRootPathFile: task is isCancelled, break.");
                    break;
                }
                if (f.isDirectory()) {
                    //根目录文件夹不扫描
                    continue;
                }
                if (!f.exists()) {
                    continue;
                }
                createDocData(f, dataList, DocumentConfig.Source.ALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "scanRootPathFile: end, size:" + dataList.size());
        return dataList;
    }

    private List<DocumentData> scanLocalPathFiles(String path) {
        Log.i(TAG, "scanLocalPathFiles: start :" + path);
        List<DocumentData> dataList = new ArrayList<>();
        scanFile(new File(DocumentUtil.PATH_EXTERNAL + "/" + path), dataList, DocumentConfig.getSourceByPath(path));
        Log.i(TAG, "scanLocalPathFiles: end :" + path + "---size:" + dataList.size());
        return dataList;
    }

    private void scanFile(File file, List<DocumentData> dataList, DocumentConfig.Source source) {
        try {
            if (!file.exists()) {
                Log.i(TAG, "scanFile: file is not exists!!!");
                return;
            }
            if (file.isDirectory()) {
                String filePath = file.getAbsolutePath();
                if (filePath.contains("emojimsg")) {
                    //过滤企业微信的emoji表情目录
                    return;
                }
                File[] files = file.listFiles();
                if(files != null) {
                    for (File f : files) {
                        if (isCancelled()) {
                            Log.i(TAG, "scanFile: task is isCancelled, break.");
                            break;
                        }
                        scanFile(f, dataList, source);
                    }
                }
            } else {
                createDocData(file, dataList, source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDocData(File file, List<DocumentData> dataList, DocumentConfig.Source source) {
        String filePath = file.getAbsolutePath();
        String suffix = DocumentUtil.getFileType(filePath);
        FormatEnum format = FormatEnum.getFormat(suffix);
        if (DocumentConfig.SUPPORT_FORMATS.contains(format)) {
            long size = file.length();
            if (size <= 0) {
                return;
            }
            int pos = filePath.lastIndexOf(File.separator);
            if (pos == -1) return;
            Log.i(TAG, "createDocData--> path:" + filePath);
            String displayName = filePath.substring(pos + 1);
            DocumentData data = new DocumentData();
            data.tittle = displayName;
            data.url = filePath;
            data.takeTime = new Date(file.lastModified());
            data.size = size;
            data.lastModifiedTime = file.lastModified();
            data.suffix = suffix;
            data.format = format.type;
            dataList.add(data);
            if (mCurSource.equals(source)) {
                progress(data);
            }
        }
    }

    private void progress(DocumentData data) {
        mProessFiles.add(data);
        sortFiles(mProessFiles);
        mDocumentBrowseCallback.onProgress(mProessFiles);
        mDocumentBrowseCallback.onPathProgress(data.url.replace(DocumentUtil.PATH_EXTERNAL, ""));
    }

    private void sortFiles(List<DocumentData> dataList) {
        Collections.sort(dataList, new Comparator<DocumentData>() {
            @Override
            public int compare(DocumentData o1, DocumentData o2) {
                return Long.compare(o2.lastModifiedTime, o1.lastModifiedTime);
            }
        });
    }
}
