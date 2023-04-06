TestCVCenter {
	*runAll {
		[
			TestCVWidget,
			TestCVWidgetKnob,
			TestMidiConnector,
			TestOscConnector,
			TestMidiConnectorElementView,
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
			TestSlidersPerGroupNumberTF
		].do(_.run)
	}
}