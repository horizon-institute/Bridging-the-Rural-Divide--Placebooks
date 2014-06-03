package org.placebooks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.placebooks.client.model.PlaceBook;
import org.placebooks.client.model.PlaceBookService;
import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.client.server.JSONServerHandler;
import org.wornchaos.client.server.Request;
import org.wornchaos.logger.Log;
import org.wornchaos.parser.Parser;
import org.wornchaos.parser.gson.GsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PlaceBookServerHandler extends JSONServerHandler
{
	private static final Parser parser = new GsonParser();
	private final Context context;

	public static PlaceBookService createServer(String host, Context context)
	{
		return (PlaceBookService) Proxy.newProxyInstance(PlaceBookService.class.getClassLoader(),
				new Class[]{PlaceBookService.class},
				new PlaceBookServerHandler(context, host));
	}

	public PlaceBookServerHandler(Context context, String host)
	{
		super(new GsonParser(), host);
		this.context = context;
	}

	@Override
	protected boolean isOnline()
	{
		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

		Log.info("Connected: " + (netInfo != null && netInfo.isConnected()));

		return netInfo != null && netInfo.isConnected();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		if (method.getName().equals("placebookPackage"))
		{
			final String id = (String) args[0];
			@SuppressWarnings("unchecked")
			final AsyncCallback<PlaceBook> callback = (AsyncCallback<PlaceBook>) args[1];
			try
			{
				final PlaceBook cached = getPlaceBook(getPlaceBookPath(id));
				if (cached != null)
				{
					callback.onSuccess(cached);
				}

				if (!isOnline())
				{
					return null;
				}

				final Request request = method.getAnnotation(Request.class);
				final String url = getURL(method, request);
				final String finalURL = url.replace("{id}", id);

				new Thread(new Runnable()
				{
					@Override
					@SuppressWarnings("unchecked")
					public void run()
					{
						try
						{
							final Object result = call(finalURL, new HashMap<String, String>(), Request.Method.GET, InputStream.class);
							final File placebookPath = getPlaceBookPath(id);
							final File cacheDir = context.getExternalCacheDir();

							final File download = new File(cacheDir, id + "_download.zip");
							download.createNewFile();

							final FileOutputStream fileOutput = new FileOutputStream(download);

							copy((InputStream) result, fileOutput);

							fileOutput.flush();
							fileOutput.close();

							unzip(new FileInputStream(download), placebookPath);

							download.delete();

							getPlaceBook(placebookPath, callback);
						}
						catch (Exception e)
						{
							callback.onFailure(e);
						}
					}
				}).start();

				return null;
			}
			catch (final Exception e)
			{
				Log.error(e);
				callback.onFailure(e);
			}

		}
		return super.invoke(proxy, method, args);
	}

	private File getPlaceBookPath(final String id) throws IOException
	{
		final URL url = new URL(hostURL);
		File file = new File(context.getExternalFilesDir(null), url.getHost());
		if (url.getPath().length() != 0)
		{
			file = new File(file, url.getPath());
		}

		return new File(file, id);
	}

	public static void getPlaceBook(final File placebookPath, final AsyncCallback<PlaceBook> callback)
	{
		try
		{
			final PlaceBook placebook = getPlaceBook(placebookPath);
			if (placebook == null)
			{
				callback.onFailure(new Exception("Not Found"));
			}
			else
			{
				callback.onSuccess(placebook);
			}
		}
		catch (Exception e)
		{
			callback.onFailure(e);
		}
	}

	private static PlaceBook getPlaceBook(final File placebookPath) throws IOException
	{
		if (placebookPath.exists())
		{
			final File placebookJSON = new File(placebookPath, "data.json");
			Log.info("Placebook json " + placebookJSON.toString());
			if (placebookJSON.exists())
			{
				Log.info("Placebook json exists");
				final FileReader reader = new FileReader(placebookJSON);
				final PlaceBook placebook = parser.parse(PlaceBook.class, reader);
				placebook.setDirectory(placebookPath.getAbsolutePath());
				return placebook;
			}
		}

		return null;
	}

	private static void copy(final InputStream input, final OutputStream output) throws IOException
	{
		final byte[] buffer = new byte[4096];
		int size;
		while ((size = input.read(buffer)) != -1)
		{
			output.write(buffer, 0, size);
		}
	}

	public static void unzip(final InputStream inputStream, File targetDir) throws Exception
	{
		targetDir.mkdirs();
		final ZipInputStream zipFile = new ZipInputStream(inputStream);
		try
		{
			ZipEntry entry;
			while ((entry = zipFile.getNextEntry()) != null)
			{
				final File targetFile = new File(targetDir, entry.getName());
				final OutputStream output = new FileOutputStream(targetFile);
				try
				{
					copy(zipFile, output);
				}
				finally
				{
					output.close();
				}
			}
		}
		finally
		{
			zipFile.close();
		}
	}
}
