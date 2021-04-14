package com.example.code;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "MessageAdapter";

    private Context mContext;
    private List<Message> messageList;
    private RecyclerView mRecyclerView;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnLongItemClickListener;

    public MessageAdapter(Context context, RecyclerView recyclerView) {
        mContext = context;
        messageList = new ArrayList<>();
        mRecyclerView = recyclerView;
    }


    public void addMsg(Message msg) {
        messageList.add(msg);
        notifyDataSetChanged();
        focusBottom(false);
    }


    public void focusBottom(final boolean smoothScroll) {
        focusBottom(smoothScroll, 300);
    }

    public void focusBottom(final boolean smoothScroll, int delayMillis) {
        if (mRecyclerView.getLayoutManager() != null && getItemCount() > 0) {
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mRecyclerView.canScrollVertically(1)) {
                        //到底了,不用滑动
                        return;
                    }
                    if (smoothScroll) {
                        mRecyclerView.smoothScrollToPosition(getItemCount() - 1);
                    } else {
                        int c = mRecyclerView.getLayoutManager().getItemCount() - 1;
                        int lastPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        if (c - lastPosition < 3) {
                            //已经显示最后一个,移动少的用滑动
                            mRecyclerView.smoothScrollToPosition(c);
                        } else {
                            mRecyclerView.scrollToPosition(c);
                        }
                    }
                }
            }, delayMillis);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (viewType == Message.MSG_TYPE.SEND.ordinal()) {
            View view = inflater.inflate(R.layout.message_send_item, parent, false);
            return new MsgItemSend(view);
        } else if (viewType == Message.MSG_TYPE.RECEIVE.ordinal()) {
            View view = inflater.inflate(R.layout.message_receive_item, parent, false);
            return new MsgItemReceive(view);
        } else {
            View view = inflater.inflate(R.layout.message_send_item, parent, false);
            return new MsgItemSend(view);
        }

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Message model = messageList.get(position);
        if (holder instanceof MsgItemSend) {
            MsgItemSend viewHeader = (MsgItemSend) holder;
            viewHeader.tv_msg.setText(model.getText());
        } else if (holder instanceof MsgItemReceive) {
            MsgItemReceive viewHeader = (MsgItemReceive) holder;
            viewHeader.tv_msg.setText(model.getText());
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(model, position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongItemClickListener != null) {
                    mOnLongItemClickListener.onItemLongClick(v, model);
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getMessageType().ordinal();
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnLongItemClickListener(OnItemLongClickListener listener) {
        this.mOnLongItemClickListener = listener;
    }


    public interface OnItemClickListener {
        void onItemClick(Message bean, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View v, Message bean);
    }

    public static class MsgItemSend extends RecyclerView.ViewHolder {
        TextView tv_msg;

        public MsgItemSend(View itemView) {
            super(itemView);
            tv_msg = itemView.findViewById(R.id.tv_msg);
        }
    }


    public static class MsgItemReceive extends RecyclerView.ViewHolder {
        TextView tv_msg;

        public MsgItemReceive(View itemView) {
            super(itemView);
            tv_msg = itemView.findViewById(R.id.tv_msg);
        }
    }


}
