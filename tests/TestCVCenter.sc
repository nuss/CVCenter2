TestCVCenter : UnitTest {
	*runAll {
		[
			TestExtCollection,
			TestExtMIDIFunc,
			TestExtFont,
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
			TestPlayPauseButton,
			TestConnectorRemoveButton,
			TestMappings,
			TestMappingSelect,
			TestMidiConnectorsEditorView,
			TestOscSelectsComboView,
			TestCVWidgetKnob,
			TestExtMIDIFunc,
			TestExtFont,
			TestExtObject,
			// Tests with asynchronous logic
			TestExtOSCFunc,
			TestExtOSCCommands
		].do(_.run)
	}
}