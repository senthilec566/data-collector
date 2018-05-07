echo "Starting StreamReceiver"

# JMX settings
if [ -z "$JMX_OPTS" ]; then
  JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false "
fi

# JMX port to use
if [  $JMX_PORT ]; then
  JMX_OPTS="$JMX_OPTS -Dcom.sun.management.jmxremote.port=$JMX_PORT "
fi

exec java $JMX_OPTS -cp "..." com.perfspeed.collector.DataCollector -conf application.conf  >> /tmp/logs/data_collector.log 2>&1