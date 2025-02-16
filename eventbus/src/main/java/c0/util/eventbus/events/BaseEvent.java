package c0.util.eventbus.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseEvent implements Event{

	private final String TIMESTAMP_FORMATTER_PATTERN = "HH:mm:ss dd-MM-yyyy";

	protected String timestamp;

	protected void captureTimestamp(){
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMATTER_PATTERN);
        timestamp = now.format(formatter);
	}

	@Override
    public String getTimestamp(){
        return timestamp;
    }

	protected Event.Type type;

	@Override
    public Type getType(){
        return this.type;
    }

	public BaseEvent(Event.Type type){
		captureTimestamp();
		this.type = type;
	}

}
