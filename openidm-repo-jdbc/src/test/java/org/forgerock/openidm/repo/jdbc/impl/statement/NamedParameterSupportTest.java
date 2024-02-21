/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2024 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.statement;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

/**
 * {@link NamedParameterSupport} test case.
 */
public class NamedParameterSupportTest {

    @Test
    public void testPrepareSqlString() throws Exception {
        var sql = NamedParameterSql.parse("SELECT * FROM hello WHERE id = ${int:foo} AND val IN (${list:bar})");

        var preparedSql = NamedParameterSupport.prepareSqlString(sql, Map.of(
                "foo", 13,
                "bar", List.of("world", "universe")));

        assertEquals(preparedSql.getSqlString(), "SELECT * FROM hello WHERE id = ? AND val IN (?, ?)");
        assertEquals(preparedSql.getParameters(), List.of(13, "world", "universe"));
    }

}