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
			TestSlidersPerGroupNumberBox,
			TestMidiInitButton,
			TestMidiConnectorRemoveButton,
			TestMappings,
			TestMappingSelect
		].do(_.run)
	}
}