package ua.epam.spring.core;

import org.apache.log4j.Logger;
import org.aspectj.weaver.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ua.epam.spring.core.aspect.StatisticsAspect;
import ua.epam.spring.core.beans.Client;
import ua.epam.spring.core.beans.Event;
import ua.epam.spring.core.loggers.DBLogger;
import ua.epam.spring.core.loggers.EventLogger;

import java.util.Map;

public class App {

    private Client client;
    private EventLogger defaultLogger;
    private Map<EventType, EventLogger> loggers;
    private static ConfigurableApplicationContext ctx;

    private static final Logger LOG = Logger.getLogger(App.class);

    public App() {
    }

    public App(Client client,
               EventLogger defaultLogger,
               Map<EventType, EventLogger> loggers) {
        this.client = client;
        this.defaultLogger = defaultLogger;
        this.loggers = loggers;
    }

    public void logEvent(EventType type, String msg) {
        EventLogger logger = loggers.get(type);
        if (logger==null) {
            logger = defaultLogger;
        }
        String message = msg.replaceAll(client.getId(), client.getFullName());
        Event event = (Event) ctx.getBean("event");
        event.setMsg(message);
        logger.logEvent(event);
    }

    public static void main(String[] args) {
        ctx = new ClassPathXmlApplicationContext("spring.xml");
        App app = (App) ctx.getBean("app");

        app.logEvent(EventType.INFO, "Some event for user 1");
        app.logEvent(EventType.ERROR, "Some event for user 2");
        app.logEvent(EventType.ERROR, "Some event for user 3");

        StatisticsAspect statistics = (StatisticsAspect)
                ctx.getBean("statisticsAspect");
//        System.out.println("Statistics:\n" + statistics);

        DBLogger dBLogger = (DBLogger) ctx.getBean("dBLogger");
        // TODO cast to target when aspect isactive
//        Advice advice = (Advice) ctx.getBean("dBLogger");
//        advice.ge

        System.out.println("Records in DB : " + dBLogger.getCount());
        System.out.println("Event with id=2: " + dBLogger.getEvent(2));

        ctx.close(); // Spring closes context, invokes destroy method of every bean.
    }
}
