package org.codehaus.jremoting.client.resolver;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.codehaus.jremoting.client.monitors.CommonsLoggingClientMonitor;
import org.codehaus.jremoting.client.monitors.Log4JClientMonitor;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.InvocationException;

public class ChainedMonitorTestCase extends MockObjectTestCase {

    public void testInvocationFailure() {
        Mock clientMonitor = mock(ClientMonitor.class);
        clientMonitor.expects(once()).method("invocationFailure").with(new Constraint[] {eq(ChainedMonitorTestCase.class), eq("1"), eq("2"), eq("3"), isA(InvocationException.class)});
        CommonsLoggingClientMonitor cm = new CommonsLoggingClientMonitor(new Log4JClientMonitor(new ConsoleClientMonitor((ClientMonitor) clientMonitor.proxy())));
        cm.invocationFailure(this.getClass(), "1", "2", "3", new InvocationException("4"));
    }

}
