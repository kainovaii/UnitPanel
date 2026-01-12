package fr.kainovaii.core.database;


import fr.kainovaii.core.Spark;
import org.javalite.activejdbc.Base;

import java.util.concurrent.Callable;

public class DB
{
    public DB() { connect(); }

    public static void connect()
    {
        if (!Base.hasConnection()) {
            SQLite.getInstance(Spark.LOGGER).connectDatabaseForCurrentThread();
        }
    }

    public static <T> T withConnection(Callable<T> task)
    {
        if (!Base.hasConnection()) {
            SQLite.getInstance(Spark.LOGGER).connectDatabaseForCurrentThread();
        }
        try {
            return task.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (Base.hasConnection()) Base.close();
        }
    }
}