TestCVCenter : UnitTest {
	*runAll {
		[
			TestExtCollection,
			TestExtMIDIFunc,
			TestExtFont,
			TestCVWidget,
			TestMidiConnector,
			TestOscConnector,
			TestMappings,
			TestCVWidgetKnob,
			TestExtObject,
			// Tests with asynchronous logic
			TestExtOSCFunc,
			TestExtOSCCommands
		].do(_.run)
	}
}