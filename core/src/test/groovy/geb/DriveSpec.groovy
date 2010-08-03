/* Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package geb

import geb.test.util.*
import spock.lang.*
import geb.error.DriveException
import org.openqa.selenium.htmlunit.HtmlUnitDriver

class DriveSpec extends Specification {

	@Shared server
	
	def setupSpec() {
		server = new TestHttpServer()
		server.start()
		server.get = { req, res ->
			res.outputStream << """
			<html>
			<body>
				<div class="url">${req.requestURL + (req.queryString ? "?${req.queryString}" : "")}</div>
				<div class="path">$req.requestURI</div>
				<div class="params">$req.parameterMap</div>
			</body>
			</html>"""
		}
		DriveSpecPageAbsoluteUrl.url = server.baseUrl
	}

	def driveWithNoArgs() {
		when:
		Browser.drive {
			go(server.baseUrl)
			assert $(".url").text() == server.baseUrl
		}
		then:
		notThrown(Exception)
	}

	def driveWithBaseUrl() {
		when:
		Browser.drive(server.baseUrl) {
			assert $(".url").text() == server.baseUrl
		}
		then:
		notThrown(Exception)
	}

	def driveWithAbsoluteUrlPage() {
		when:
		Browser.drive(DriveSpecPageAbsoluteUrl) {
			assert $(".url").text() == server.baseUrl
		}
		then:
		notThrown(Exception)
	}

	def driveWithRelativeUrlPage() {
		when:
		Browser.drive(server.baseUrl, DriveSpecPageRelativeUrl) {
			assert $(".url").text() == server.baseUrl + DriveSpecPageRelativeUrl.url
		}
		then:
		notThrown(Exception)
	}

	def driveWithDriver() {
		when:
		Browser.drive(new HtmlUnitDriver()) {
			go(server.baseUrl)
			assert $(".url").text() == server.baseUrl
		}
		then:
		notThrown(Exception)
	}

	def driveWithDriverAndPage() {
		when:
		Browser.drive(new HtmlUnitDriver(), DriveSpecPageAbsoluteUrl) {
			assert $(".url").text() == server.baseUrl
		}
		then:
		notThrown(Exception)
	}

	def driveWithDriverAndBaseUrl() {
		when:
		Browser.drive(new HtmlUnitDriver(), server.baseUrl) {
			go()
			assert $(".url").text() == server.baseUrl
		}
		then:
		notThrown(Exception)
	}
	
	def driveException() {
		when:
		Browser.drive {
			throw new Error()
		}
		then:
		thrown(DriveException)
	}

	def cleanupSpec() {
		server.stop()
	}

}

class DriveSpecPageAbsoluteUrl extends Page {
	static url
}

class DriveSpecPageRelativeUrl extends Page {
	static url = "a/b"
}