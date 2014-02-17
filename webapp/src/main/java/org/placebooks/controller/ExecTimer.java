package org.placebooks.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecTimer extends Thread
{

	private class ExecConsumer extends Thread
	{

		private InputStream is = null;
		private boolean kill;

		public ExecConsumer(final InputStream is)
		{
			this.is = is;
			kill = false;
		}

		public void kill()
		{
			kill = true;
		}

		@Override
		public void run()
		{
			try
			{
				final InputStreamReader isr = new InputStreamReader(is);
				final BufferedReader out = new BufferedReader(isr);
				String line = "";
				while (!kill && (line = out.readLine()) != null)
				{
					System.out.println("[output] " + line);
				}

				if (kill)
				{
					System.out.println("Process killed");
					isr.close();
				}

			}
			catch (final Throwable e)
			{
				System.out.println(e.toString());
			}
		}

	}

	private long time;

	private String command;

	public ExecTimer(final long time, final String command)
	{
		this.time = time;
		this.command = command;
	}

	@Override
	public void run()
	{
		try
		{
			final Process p = Runtime.getRuntime().exec(command);
			final Thread t1 = new ExecConsumer(p.getErrorStream());
			t1.start();
			final Thread t2 = new ExecConsumer(p.getInputStream());
			t2.start();

			try
			{
				System.out.println("Waiting for process... allowing " + time + " millis");
				sleep(time);
			}
			catch (final InterruptedException e)
			{
				System.out.println(e.toString());
			}
			((ExecConsumer) t1).kill();
			((ExecConsumer) t2).kill();
			p.destroy();
		}
		catch (final IOException e)
		{
			System.out.println(e.toString());
		}
	}

}
