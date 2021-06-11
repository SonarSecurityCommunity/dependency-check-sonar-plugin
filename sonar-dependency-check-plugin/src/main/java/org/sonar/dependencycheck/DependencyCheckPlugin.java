/*
 * Dependency-Check Plugin for SonarQube
 * Copyright (C) 2015-2021 dependency-check
 * philipp.dallig@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.dependencycheck;

import java.util.Arrays;

import org.sonar.api.Plugin;
import org.sonar.dependencycheck.base.DependencyCheckMeasureComputer;
import org.sonar.dependencycheck.base.DependencyCheckMetrics;
import org.sonar.dependencycheck.page.DependencyCheckReportPage;
import org.sonar.dependencycheck.rule.KnownCveRuleDefinition;
import org.sonar.dependencycheck.rule.NeutralLanguage;
import org.sonar.dependencycheck.rule.NeutralProfile;

public final class DependencyCheckPlugin implements Plugin {

    @Override
    public void define(Context context) {
        context.addExtensions(Arrays.asList(
                DependencyCheckSensor.class,
                DependencyCheckMetrics.class,
                DependencyCheckMeasureComputer.class,
                NeutralProfile.class,
                NeutralLanguage.class,
                KnownCveRuleDefinition.class,
                DependencyCheckReportPage.class));
        context.addExtensions(DependencyCheckConfiguration.getPropertyDefinitions());
    }
}
