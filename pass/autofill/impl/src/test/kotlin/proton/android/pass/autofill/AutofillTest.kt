/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill

import org.junit.Test

class AutofillTest {

    @Test
    fun `can autofill news ycombinator com firefox (focus on first username)`() {
        runAutofillTest("firefox_news.ycombinator.com_firstusername.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on first password)`() {
        runAutofillTest("firefox_news.ycombinator.com_firstpassword.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on second username)`() {
        runAutofillTest("firefox_news.ycombinator.com_secondusername.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on second password)`() {
        runAutofillTest("firefox_news.ycombinator.com_secondpassword.json")
    }

    @Test
    fun `can autofill account dyn com chrome`() {
        runAutofillTest("chrome_account.dyn.com.json")
    }

    @Test
    fun `can autofill protonmail app`() {
        runAutofillTest("app_ch.protonmail.android.json")
    }

    @Test
    fun `can autofill instagram app`() {
        runAutofillTest("app_com.instagram.android.json")
    }
}