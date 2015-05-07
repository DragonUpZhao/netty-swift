package com.letv.psp.swift.httpd;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.netty.buffer.ChannelBuffer;

public class SwiftInputStream extends InputStream {
	private final ChannelBuffer buffer;
	
	public SwiftInputStream(ChannelBuffer buffer){
		this.buffer=buffer;
		this.buffer.markReaderIndex();
	}
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		if(!buffer.readable()){
			return -1;
		}
		return buffer.readByte()&0xff;
	}

}
