/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.jaeksoft.opensearchserver.front.accounts.webcrawl;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.Message;
import com.jaeksoft.opensearchserver.front.accounts.AccountTransaction;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.web.WebCrawlDefinition;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class WebCrawlEditTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/crawlers/web/edit.ftl";

	private final WebCrawlsService webCrawlsService;
	private final WebCrawlRecord webCrawlRecord;

	public WebCrawlEditTransaction(final Components components, final UUID accountId, final UUID webCrawlUuid,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		super(components, accountId, request, response);

		this.webCrawlsService = components.getWebCrawlsService();
		webCrawlRecord = webCrawlsService.read(accountRecord.id, webCrawlUuid);
		if (webCrawlRecord == null)
			throw new NotFoundException("Web crawl not found: " + webCrawlUuid);
		
		request.setAttribute("webCrawlRecord", webCrawlRecord);
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	public void delete() throws IOException, ServletException {
		final String crawlName = request.getParameter("crawlName");
		if (webCrawlRecord.name.equals(crawlName)) {
			webCrawlsService.remove(accountRecord.id, webCrawlRecord.getUuid());
			addMessage(Message.Css.success, null, "Crawl \"" + webCrawlRecord.name + "\" deleted");
			response.sendRedirect("/accounts/" + accountRecord.id + "/crawlers/web");
			return;
		} else
			addMessage(Message.Css.warning, null, "Please confirm the name of the crawl to delete");
		doGet();
	}

	public void save() throws IOException {
		final String crawlName = request.getParameter("crawlName");
		final String entryUrl = request.getParameter("entryUrl");
		final Integer maxDepth = getRequestParameter("maxDepth", null);
		final WebCrawlDefinition.Builder webCrawlDefBuilder =
				WebCrawlDefinition.of().setEntryUrl(entryUrl).setMaxDepth(maxDepth);
		webCrawlsService.save(accountRecord.id,
				WebCrawlRecord.of(webCrawlRecord).name(crawlName).crawlDefinition(webCrawlDefBuilder.build()).build());
		response.sendRedirect("/accounts/" + accountRecord.id + "/crawlers/web/" + webCrawlRecord.getUuid());
	}

}
