package net.bbmsoft.bbm.utils.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class StringOutputStream extends ByteArrayOutputStream {

	private volatile Consumer<String> stringConsumer;
	private volatile Charset charset;

	public StringOutputStream() {
		this(null, Charset.defaultCharset());
	}

	public StringOutputStream(Consumer<String> stringConsumer) {
		this(stringConsumer, Charset.defaultCharset());
	}

	public StringOutputStream(Consumer<String> stringConsumer, Charset charset) {
		this.setStringConsumer(stringConsumer);
		this.setCharset(charset);
	}

	@Override
	public void flush() {

		Consumer<String> stringConsumer = this.getStringConsumer();

		if (stringConsumer == null) {
			this.reset();
			return;
		}

		byte[] bytes = this.toByteArray();
		this.reset();

		Charset charset = this.charset;

		String string = new String(bytes, charset != null ? charset : Charset.defaultCharset());

		stringConsumer.accept(string);
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public Consumer<String> getStringConsumer() {
		return stringConsumer;
	}

	public void setStringConsumer(Consumer<String> stringConsumer) {
		this.stringConsumer = stringConsumer;
	}

}
