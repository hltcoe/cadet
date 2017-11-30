/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;

import edu.jhu.hlt.cadet.ConfigManager;

public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 283945342041275852L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Config config = ConfigManager.getInstance().getConfig();

        // Documentation for ConfigRenderOptions available here:
        //   https://typesafehub.github.io/config/latest/api/com/typesafe/config/ConfigRenderOptions.html
        ConfigRenderOptions options =
            ConfigRenderOptions.defaults().setComments(false).setOriginComments(false);

        out.println(config.root().render(options));
    }
}
