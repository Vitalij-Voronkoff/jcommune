/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.test.utils.page;

import org.jtalks.jcommune.test.utils.Users;
import org.jtalks.jcommune.test.utils.exceptions.ValidationException;
import org.jtalks.jcommune.test.utils.exceptions.WrongResponseException;
import org.jtalks.jcommune.test.utils.model.User;
import org.jtalks.jcommune.web.controller.UserController;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author Mikhail Stryzhonok
 */
class PageUsers extends Users {

    static final def BINDING_RESULT_ATTRIBUTE_NAME = BindingResult.MODEL_KEY_PREFIX + "newUser"

    @Override
    def signIn(User user) {
        def result = mockMvc.perform(post('/login')
                .param('userName', user.username)
                .param('password', user.password)
                .param('referer', '/'))
                .andReturn()

        assertViewEquals(result, "redirect:/")
        return result.request.session
    }

    @Override
    def singUp(User user) {
        def resultActions = mockMvc.perform(post('/user/new')
                .param('userDto.username', user.username)
                .param('userDto.email', user.email)
                .param('userDto.password', user.password)
                .param('passwordConfirm', user.confirmation)
                .param('honeypotCaptcha', user.honeypot))

        MvcResult result = resultActions.andReturn();
        assertNoErrors(result)
        assertViewEquals(result, UserController.AFTER_REGISTRATION)
        return user.username
    }

    @Override
    def assertNoErrors(MvcResult mvcResult) {
        def mav = mvcResult.modelAndView
        def result = mav.model.get(BINDING_RESULT_ATTRIBUTE_NAME) as BindingResult
        if (result.hasErrors()) {
            def ex = new ValidationException()
            for (def error in result.allErrors) {
                ex.addDefaultErrorMessage(error.defaultMessage)
            }
            throw ex
        }
    }

    def assertViewEquals(MvcResult mvcResult, String expectedViewName) {
        def mav = mvcResult.modelAndView
        if (expectedViewName != mav.viewName) {
            throw new WrongResponseException(expectedViewName,
                    mvcResult.modelAndView.viewName)
        }
    }
}
