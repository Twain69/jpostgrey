package interfaces;

import com.flegler.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.InputRecord;

public interface DataFetcher {
	public int getDuration(InputRecord inputRecord)
			throws InputRecordNotFoundException;

}
