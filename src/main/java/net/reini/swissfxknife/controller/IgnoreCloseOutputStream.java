package net.reini.swissfxknife.controller;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

final class IgnoreCloseOutputStream extends FilterOutputStream {

    IgnoreCloseOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void close() throws IOException {
        // ignored
    }
}
