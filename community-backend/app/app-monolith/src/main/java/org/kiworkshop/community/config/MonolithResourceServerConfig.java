package org.kiworkshop.community.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.kiworkshop.community.common.domain.Role;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.GenericFilterBean;

@Configuration
@EnableResourceServer
public class MonolithResourceServerConfig extends ResourceServerConfigurerAdapter {


  public static class JwtDecodeFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      // TODO: 2020/08/10 Decode X-Community-Token and insert token to Authorization header
      // Authorization: Bearer access-token
      chain.doFilter(request, response);
    }
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
        .addFilterBefore(new JwtDecodeFilter(), AbstractPreAuthenticatedProcessingFilter.class)
        .authorizeRequests()
        .antMatchers(HttpMethod.GET, "/user-resources/me").authenticated()
        .antMatchers(HttpMethod.GET, "/notices").permitAll()
        .antMatchers(HttpMethod.GET, "/notices/{\\d}").permitAll()
      .and()
        .authorizeRequests()
        .antMatchers("/**").hasAuthority(Role.ROLE_ADMIN.name())
      .and()
    ;
  }
}
