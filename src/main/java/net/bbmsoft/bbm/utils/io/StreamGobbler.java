package net.bbmsoft.bbm.utils.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Consumer;

public class StreamGobbler {

	private StreamGobbler() {
	}

	public static Thread get(InputStream in, Consumer<String> out) {
		return new ConsumingStreamGobbler(in, out);
	}

	public static Thread get(InputStream in, OutputStream out) {
		return new StreamToStreamGobbler(in, out, false);
	}

	public static Thread get(InputStream in, OutputStream out, boolean autoFlush) {
		return new StreamToStreamGobbler(in, out, autoFlush);
	}

	private static class ConsumingStreamGobbler extends Thread {

		private final InputStream in;
		private final Consumer<String> out;

		public ConsumingStreamGobbler(InputStream in, Consumer<String> out) {
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {

			try (Scanner sc = new Scanner(this.in)) {
				while (sc.hasNextLine()) {
					String nextLine = sc.nextLine();
					String trim = nextLine.trim();
					if (!trim.isEmpty()) {
						String replace = trim.replace("\n", "").replace("\r", "");
						this.out.accept(replace);
					}
				}
			}

		}
	}

	private static class StreamToStreamGobbler extends Thread {

		private final InputStream in;
		private final OutputStream out;
		private final boolean autoFlush;

		public StreamToStreamGobbler(InputStream in, OutputStream out, boolean autoFlush) {
			this.in = in;
			this.out = out;
			this.autoFlush = autoFlush;
		}

		@Override
		public void run() {

			try (Scanner sc = new Scanner(this.in); PrintStream ps = new PrintStream(this.out)) {
				while (sc.hasNextLine()) {
					String nextLine = sc.nextLine();
					String trim = nextLine.trim();
					if (!trim.isEmpty()) {
						String replace = trim.replace("\n", "").replace("\r", "");
						ps.println(replace);
						if (this.autoFlush) {
							ps.flush();
						}
					}
				}
			}
		}
	}
}
