package com.azx.gateway.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

@Component
public class RouteFilter extends ZuulFilter {

	@Autowired
	TokenStore tokenStore;

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();

		if (ctx.getRequest().getHeader("Authorization") != null
				&& ctx.getRequest().getRequestURL().indexOf("oauth/token") == -1) {
			String header = ctx.getRequest().getHeader("Authorization");
			String token = header.substring(7, header.length());
			OAuth2AccessToken existingAccessToken = tokenStore.readAccessToken(token);
			Map<String, Object> tokenAddInfo = existingAccessToken.getAdditionalInformation();
			ctx.addZuulRequestHeader("userName", tokenAddInfo.get("userName").toString());
		}
		return null;
	}
}
