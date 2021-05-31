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
package org.sonar.dependencycheck.rule;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.dependencycheck.base.DependencyCheckConstants;

public class NeutralProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile dependencyCheckWay = context.createBuiltInQualityProfile("Neutral", DependencyCheckConstants.LANGUAGE_KEY);
        dependencyCheckWay.activateRule(DependencyCheckConstants.REPOSITORY_KEY, DependencyCheckConstants.RULE_KEY);
        dependencyCheckWay.activateRule(DependencyCheckConstants.REPOSITORY_KEY,
                DependencyCheckConstants.RULE_KEY_WITH_SECURITY_HOTSPOT);
        dependencyCheckWay.done();
    }
}
