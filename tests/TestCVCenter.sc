TestCVCenter {
	*runAll {
		[
			TestCVWidget,
			TestCVWidgetKnob,
			TestMidiConnector,
			TestOscConnector,
			TestConnectorElementView,
			TestMidiConnectorNameField,
			TestMidiConnectorSelect,
			TestMidiLearnButton,
			TestMidiSrcSelect,
			TestMidiChanField,
			TestMidiCtrlField,
			TestMidiModeSelect,
			TestMidiZeroNumberBox,
			TestSnapDistanceNumberBox,
			TestMidiResolutionNumberBox,
			TestSlidersPerGroupNumberTF,
			TestMidiInitButton
		].do(_.run)
	}
}