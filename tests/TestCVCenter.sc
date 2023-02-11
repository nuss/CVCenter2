TestCVCenter {
	*runAll {
		[
			TestCVWidget,
			TestCVWidgetKnob,
			TestMidiConnector,
			TestOscConnector,
			TestMidiConnectorSelect,
			TestMidiLearnButton,
			TestMidiSrcSelect,
			TestMidiChanField,
			TestMidiCtrlField,
			TestMidiModeSelect,
			TestMidiMeanNumberBox,
			TestSoftWithinNumberBox,
			TestMidiResolutionNumberBox,
			TestSlidersPerBankNumberTF
		].do(_.run)
	}
}