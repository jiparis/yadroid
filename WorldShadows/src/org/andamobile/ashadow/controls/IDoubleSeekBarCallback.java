package org.andamobile.ashadow.controls;

/**
 * Callback interface for DoubleSeekBar subclasses. Each {@link DoubleSeekBar}
 * has one IDoubleSeekBarCallback to notify the model about changes as well as
 * to get the actual minimum and maximum value.<br>
 * <strong>Important:</strong> The DoubleSeekBar will not display any changes if
 * the callback does not invoke, somehow, the
 * {@link DoubleSeekBar#updateStartValue(float)} or
 * {@link DoubleSeekBar#updateEndValue(float)} method.
 */
public interface IDoubleSeekBarCallback {
	/**
	 * Called by the {@link DoubleSeekBar} to announce updates of the slider for
	 * the minimum value. The update, however, will not be displayed
	 * automatically. Therefore, the implementation of this method needs to make
	 * sure that the {@link DoubleSeekBar#updateStartValue(float)} is called
	 * somehow. The purpose of this method is, therefore, to update a model with
	 * the new value, which may apply constraints to the value before updating
	 * the {@link DoubleSeekBar}'s view.
	 * 
	 * @param newValue
	 *            the new relative minimum value of the range [0,1[ (always
	 *            smaller than the current maximum value)
	 */
	public void onMinValueChange(float newValue);

	/**
	 * Called by the {@link DoubleSeekBar} to announce updates of the slider for
	 * the maximum value. The update, however, will not be displayed
	 * automatically. Therefore, the implementation of this method needs to make
	 * sure that the {@link DoubleSeekBar#updateEndValue(float)} is called
	 * somehow. The purpose of this method is, therefore, to update a model with
	 * the new value, which may apply constraints to the value before updating
	 * the {@link DoubleSeekBar}'s view.
	 * 
	 * @param newValue
	 *            the new relative maximum value of the range ]0,1] (always
	 *            greater than the current minimum value)
	 */
	public void onMaxValueChange(float newValue);

	/**
	 * Gets the current relative minimum value. This value should be retrieved
	 * from a model or be stored internally (initial default value would be 0,
	 * updated by {@link IDoubleSeekBarCallback#onMinValueChange(float)}
	 * method).
	 * 
	 * @return the current minimum value
	 */
	public float getMinValue();

	/**
	 * Gets the current relative maximum value. This value should be retrieved
	 * from a model or be stored internally (initial default value would be 1,
	 * updated by {@link IDoubleSeekBarCallback#onMaxValueChange(float)}
	 * method).
	 * 
	 * @return the current minimum value
	 */
	public float getMaxValue();

	/**
	 * Gets the current label for the minimum value. To be retrieved from a
	 * model or to be formatted internally.
	 * 
	 * @return the label String for the minimum value
	 */
	public String getMinLabel();

	/**
	 * Gets the current label for the maximum value. To be retrieved from a
	 * model or to be formatted internally.
	 * 
	 * @return the label String for the maximum value
	 */
	public String getMaxLabel();
}
