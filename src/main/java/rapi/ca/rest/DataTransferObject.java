package rapi.ca.rest;

import java.io.Serializable;

public interface DataTransferObject<T> extends Serializable {

	public T getId();
}
