TestCVCenter {
	*runAll {
		[
			TestCVWidget,
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
			TestMappingSelect,
			TestMidiConnectorsEditorView,
			TestOscSelectsComboView,
			TestCVWidgetKnob,
		].do(_.run)
	}
}