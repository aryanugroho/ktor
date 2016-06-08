package org.jetbrains.ktor.tests.http

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.auth.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.testing.*
import org.jetbrains.ktor.tests.*
import org.junit.*
import kotlin.test.*

class URLBuilderTest {
    @Test
    fun testPathNoFirstSlash() {
        val s = url {
            encodedPath = "a/b"
        }

        assertEquals("http://localhost/a/b", s.toString())
    }

    @Test
    fun testPathFirstSlash() {
        val s = url {
            encodedPath = "/a/b"
        }

        assertEquals("http://localhost/a/b", s.toString())
    }

    @Test
    fun testPathFunctionVararg() {
        val s = url {
            path("a", "b")
        }

        assertEquals("http://localhost/a/b", s.toString())
    }

    @Test
    fun testPathFunctionList() {
        val s = url {
            path(listOf("a", "b"))
        }

        assertEquals("http://localhost/a/b", s.toString())
    }

    @Test
    fun testPathWithSpace() {
        assertEquals("http://localhost/a%20b/c", url { path("a b", "c") })
    }

    @Test
    fun testPathWithPlus() {
        assertEquals("http://localhost/a+b/c", url { path("a+b", "c") })
    }

    @Test
    fun testPort() {
        assertEquals("http://localhost/", url { port = 80 })
        assertEquals("http://localhost:8080/", url { port = 8080 })
        assertEquals("https://localhost:80/", url { protocol = URLProtocol.HTTPS })
        assertEquals("https://localhost/", url { protocol = URLProtocol.HTTPS; port = 443 })
    }

    @Test
    fun testUserCredentials() {
        assertEquals("http://user:pass@localhost/", url { user = UserPasswordCredential("user", "pass") })
        assertEquals("http://user%20name:pass+@localhost/", url { user = UserPasswordCredential("user name", "pass+") })
    }

    @Test
    fun testParameters() {
        assertEquals("http://localhost/?p1=v1", url { parameters.append("p1", "v1") })
        assertEquals("http://localhost/?p1=v1&p1=v2", url { parameters.appendAll("p1", listOf("v1", "v2")) })
        assertEquals("http://localhost/?p1=v1&p2=v2", url { parameters.append("p1", "v1"); parameters.append("p2", "v2") })
    }

    @Test
    fun testParametersSpace() {
        assertEquals("http://localhost/?p1=v1+space", url { parameters.append("p1", "v1 space") })
    }

    @Test
    fun testParametersPlus() {
        assertEquals("http://localhost/?p1=v1%2B.plus", url { parameters.append("p1", "v1+.plus") })
    }

    @Test
    fun testParametersSpaceInParamName() {
        assertEquals("http://localhost/?p1+space=v1", url { parameters.append("p1 space", "v1") })
    }

    @Test
    fun testParametersPlusInParamName() {
        assertEquals("http://localhost/?p1%2B.plus=v1", url { parameters.append("p1+.plus", "v1") })
    }

    @Test
    fun testParametersEqInParamName() {
        assertEquals("http://localhost/?p1%3D.eq=v1", url { parameters.append("p1=.eq", "v1") })
    }

    @Test
    fun testFragment() {
        assertEquals("http://localhost/?p=v#a", url {
            parameters.append("p", "v")
            fragment = "a"
        })
        assertEquals("http://localhost/#a", url {
            fragment = "a"
        })
        assertEquals("http://localhost/#a%20+%20b", url {
            fragment = "a + b"
        })
    }

    @Test
    fun testWithApplication() {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) { call ->
                assertEquals("http://my-host/path%20/to?p=v", call.url())
                assertEquals("http://my-host/path%20/to?p=v", call.url {
                    assertEquals("my-host", host)
                    assertEquals("/path%20/to", encodedPath)
                    assertEquals("v", parameters.build()["p"])
                })
            }

            handleRequest(HttpMethod.Get, "/path%20/to?p=v") {
                addHeader(HttpHeaders.Host, "my-host")
            }
        }
    }

    @Test
    fun testWithApplicationAndPort() {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) { call ->
                assertEquals("http://my-host:8080/path%20/to?p=v", call.url())
                assertEquals("http://my-host:8080/path%20/to?p=v", call.url {
                    assertEquals(8080, port)
                    assertEquals("my-host", host)
                    assertEquals("/path%20/to", encodedPath)
                    assertEquals("v", parameters.build()["p"])
                })
            }

            handleRequest(HttpMethod.Get, "/path%20/to?p=v") {
                addHeader(HttpHeaders.Host, "my-host:8080")
            }
        }
    }
}