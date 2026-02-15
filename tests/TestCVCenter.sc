TestCVCenter : UnitTest {
	*runAll {
		[
			TestCVWidget,
			TestMidiConnector,
			TestOscConnector,
			TestConnectorElementView,
			TestConnectorNameField,
			TestConnectorSelect,
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
			TestConnectorRemoveButton,
			TestMappings,
			TestMappingSelect,
			TestMidiConnectorsEditorView,
			TestOscSelectsComboView,
			TestCVWidgetKnob,
		].do(_.run)
	}
}