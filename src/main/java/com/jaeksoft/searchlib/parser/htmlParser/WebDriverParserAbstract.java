/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser.htmlParser;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterString;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public abstract class WebDriverParserAbstract extends HtmlDocumentProvider {

	protected WebDriverParserAbstract(HtmlParserEnum parserEnum) {
		super(parserEnum);
	}

	protected abstract BrowserDriver<?> getWebDriver() throws InstantiationException, IllegalAccessException;

	@Override
	protected DomHtmlNode getDocument(String charset, InputStream inputStream) throws IOException {
		throw new IOException("Not allowed");
	}

	@Override
	protected HtmlNodeAbstract<?> getDocument(String htmlSource) throws IOException {
		throw new IOException("Not allowed");
	}

	@Override
	protected HtmlNodeAbstract<?> getDocument(String charset, StreamLimiter streamLimiter)
			throws SAXException, IOException, ParserConfigurationException, SearchLibException {
		BrowserDriver<?> webDriver = null;
		StringReader reader = null;
		StreamLimiter newStreamLimiter = null;
		try {
			webDriver = getWebDriver();
			String source = webDriver.getSourceCode(streamLimiter.getOriginURL());
			String newCharset = StringUtils.charsetDetector(source.getBytes());
			if (newCharset == null)
				newCharset = charset;
			newStreamLimiter =
					new StreamLimiterString(source, streamLimiter.getLimit(), streamLimiter.getOriginalFileName(),
							streamLimiter.getOriginURL());

			HtmlDocumentProvider htmlProvider =
					HtmlParserEnum.BestScoreParser.getHtmlParser(newCharset, newStreamLimiter, true);
			return htmlProvider.getRootNode();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IOException(e);
		} finally {
			IOUtils.close(webDriver, reader, newStreamLimiter);
		}
	}

	@Override
	public boolean isXPathSupported() {
		return true;
	}

	@Override
	public String generateSource() {
		throw new RuntimeException("Not allowed");
	}
}
