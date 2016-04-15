/**
 *
 * Copyright (c) 2016 Divroll and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.events.LoginEvents;
import com.divroll.webdash.client.shared.Token;
import com.divroll.webdash.client.shared.User;
import com.divroll.webdash.client.resources.proxy.TokensResource;
import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.resources.proxy.UserResource;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import elemental.client.Browser;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.restlet.client.resource.Result;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
@Templated("#content")
@Page(role = DefaultPage.class)
public class LoginPage extends Composite {

    @DataField
    @Inject
    Footer footer;

    @DataField
    @Inject
    TextBox username;

    @DataField
    @Inject
    TextBox password;

    @DataField
    @Inject
    Button login;

    @Inject
    TransitionTo<AssetsPage> assetsPage;

    @Inject
    TokensResource tokensResource;

    @Inject
    UserResource userResource;

    @Inject
    LoggedInUser loggedInUser;

    @Inject
    LoginEvents loginEvents;

    @EventHandler ("login")
    public void login (ClickEvent event){
        final String username = this.username.getText();
        String password = this.password.getText();
        // TODO: Validation
        tokensResource.signin(username, password, new Result<Token>() {
            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Token failure: " + throwable.getMessage());
            }
            @Override
            public void onSuccess(final Token token) {
                userResource.getUser(String.valueOf(token.getUserId()), token.getToken(), new Result<User>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        Window.alert("User failure: " + throwable.getMessage());
                    }
                    @Override
                    public void onSuccess(User user) {
                        if(user != null){
                            Browser.getWindow().getConsole().log("User id: " + user.getId());
                            loggedInUser.setUser(user);
                            loggedInUser.setToken(token.getToken());
                            loginEvents.fireLoginEvent(user);
                            assetsPage.go();
                        } else {
                            Window.alert("Failed to get user");
                        }
                    }
                });
            }
        });
    }


}






