package com.coocaa.tvpi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Descriptions
 * 
 * @version 2014-6-5
 * @author luguo3000
 * @since JDK1.6
 * 
 */
public class WebIconFinder {

	private static final Pattern[] ICON_PATTERNS = new Pattern[] {
			Pattern.compile( "rel=[\"']shortcut icon[\"'][^\r\n>]+?((?<=href=[\"']).+?(?=[\"']))" ),
			Pattern.compile( "((?<=href=[\"']).+?(?=[\"']))[^\r\n<]+?rel=[\"']shortcut icon[\"']" ) };
	private static final Pattern HEAD_END_PATTERN = Pattern.compile( "</head>" );

	// 获取稳定url
	private static String getFinalUrl( String urlString ) {
		HttpURLConnection connection = null;
		try {
			connection = getConnection( urlString );
			connection.connect();

			// 是否跳转，若跳转则跟踪到跳转页面
			if ( connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
					|| connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP ) {
				String location = connection.getHeaderField( "Location" );
				if ( !location.contains( "http" ) ) {
					location = urlString + "/" + location;
				}
				return location;
			}
		}
		catch ( Exception e ) {
			System.err.println( "获取跳转链接超时，返回原链接" + urlString );
		}
		finally {
			if ( connection != null )
				connection.disconnect();
		}
		return urlString;
	}

	// 获取Icon地址
	public static String getIconUrlString( String urlString ) throws MalformedURLException {

		urlString = getFinalUrl( urlString );
		URL url = new URL( urlString );
		String iconUrl = url.getProtocol() + "://" + url.getHost() + "/favicon.ico";// 保证从域名根路径搜索
		if ( hasRootIcon( iconUrl ) )
			return iconUrl;
		
		return getIconUrlByRegex( urlString );
	}

	// 判断在根目录下是否有Icon
	private static boolean hasRootIcon( String urlString ) {
		HttpURLConnection connection = null;

		try {
			connection = getConnection( urlString );
			connection.connect();
			return HttpURLConnection.HTTP_OK == connection.getResponseCode() && connection.getContentLength() > 0;
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		finally {
			if ( connection != null )
				connection.disconnect();
		}
	}

	// 从html中获取Icon地址
	private static String getIconUrlByRegex( String urlString ) {

		try {
			String headString = getHead( urlString );
			
			for ( Pattern iconPattern : ICON_PATTERNS ) {
				Matcher matcher = iconPattern.matcher( headString );
				
				if ( matcher.find() ) {
					String iconUrl = matcher.group( 1 );
					if ( iconUrl.contains( "http" ) )
						return iconUrl;

					if ( iconUrl.charAt( 0 ) == '/' ) {//判断是否为相对路径或根路径
						URL url = new URL( urlString );
						iconUrl = url.getProtocol() + "://" + url.getHost() + iconUrl;
					}
					else {
						iconUrl = urlString + "/" + iconUrl;
					}
					return iconUrl;
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	// 爬取一级域名
	private static Set<String> getUrls( String urlString ) {

		Set<String> urlSet = new HashSet<String>();
		Pattern pattern = Pattern
				.compile( "(http|https)://www\\..+?\\.(aero|arpa|biz|com|coop|edu|gov|info|int|jobs|mil|museum|name|nato|net|org|pro|travel|[a-z]{2})" );
		Matcher matcher = pattern.matcher( getHtml( urlString ) );
		
		while ( matcher.find() ) {
			urlSet.add( matcher.group() );
		}

		return urlSet;
	}

	// 获取截止到head尾标签的文本
	private static final String getHead( String urlString ) {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		
		try {
			connection = getConnection( urlString );
			connection.connect();
			reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );

			String line = null;
			StringBuilder headBuilder = new StringBuilder();
			while ( ( line = reader.readLine() ) != null ) {
				Matcher matcher = HEAD_END_PATTERN.matcher( line );
				if ( matcher.find() )
					break;
				headBuilder.append( line );
			}

			return headBuilder.toString();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if ( reader != null )
					reader.close();
				if ( connection != null )
					connection.disconnect();
			}
			catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	// 获取html页面文本
	private static final String getHtml( String urlString ) {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		
		try {
			connection = getConnection( urlString );
			connection.connect();
			reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );

			String line = null;
			StringBuilder htmlBuilder = new StringBuilder();
			while ( ( line = reader.readLine() ) != null ) {
				htmlBuilder.append( line );
			}

			return htmlBuilder.toString();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if ( reader != null )
					reader.close();
				if ( connection != null )
					connection.disconnect();
			}
			catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	// 获取一个连接
	private static HttpURLConnection getConnection( String urlString ) throws IOException {
		URL url = new URL( urlString );
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setInstanceFollowRedirects( false );
		connection.setConnectTimeout( 3000 );
		connection.setReadTimeout( 3000 );
		connection
				.setRequestProperty( "User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36" );
		return connection;
	}
}
