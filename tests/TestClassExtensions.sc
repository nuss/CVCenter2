TestExtCollection : UnitTest {
	var testArr;
	var testDict;

	setUp {
		testArr = [3, 5, "ewr", \teqre, 4, 5, 1];
		testDict = IdentityDictionary[\a -> (
			a: [3, 6, Dictionary["abc" -> IdentityDictionary[\d -> 6]]]
		)];
	}

	test_includesAllEqual {
		var subArrTrue = [3, "ewr", 1, 4];
		var subArrFalse = subArrTrue ++ 7;
		var result = testArr.includesAllEqual(subArrTrue);
		this.assert(result, "All elements in subArray should be contained in testArr checked by equality.");
		result = testArr.includesAllEqual(subArrFalse);
		this.assert(result.not, "Adding an element to subArrTrue that is not contained in testArr based on equality should make includesAllEqual fail.")
	}

	test_includesNone {
		var subArrTrue = [13, 56, "ewr"];
		var subArrFalse = [2, 5, "ewr"];
		var result = testArr.includesNone(subArrTrue);
		this.assert(result, "None of the elements in subArrTrue should be contained in testArr checked by identity.");
		result = testArr.includesNone(subArrFalse);
		this.assert(result.not, "Some of the elements in subArrFalse should be contained in testArr checked by identity.");
	}

	test_includesNonEqual {
		var subArrTrue = [13, 56, "rytrewrf"];
		var subArrFalse = [13, 56, "ewr"];
		var result = testArr.includesNoneEqual(subArrTrue);
		this.assert(result, "No elements in subArrTrue should be contained in testArr checked by equality.");
		result = testArr.includesNoneEqual(subArrFalse);
		this.assert(result.not, "Some of the elements in subArrFalse should be contained in testArr checked by equality.");
	}

	test_depth {
		var result = [].depth;
		this.assertEquals(result, 0, "An empty array should return a depth of 0.");
		result = [3, 4, 5].depth;
		this.assertEquals(result, 0, "An array containing three numbers should return a depth of 0.");
		result = ().depth;
		this.assertEquals(result, 0, "An empty events should return a depth of 0.");
		result = (a: 1, b: 2, c: 3).depth;
		this.assertEquals(result, 0, "An Event containing three numbers should return a depth of 0.");
		result = testDict.depth;
		this.assertEquals(result, 4, "The depth of testDict should by default be 4.");
		testDict[\a][\a][2] = nil;
		result = testDict.depth;
		this.assertEquals(result, 2, "After setting testDict['a']['a'][2] to nil testDict.depth should return 2.");
		testDict.clear;
		result = testDict.depth;
		this.assertEquals(result, 0, "After calling testDict.clear testDict.depth should return 0.");
	}
}

TestExtOSCFunc : UnitTest {
	var widget1, widget2;

	setUp {
		widget1 = CVWidgetKnob(\test1);
		widget2 = CVWidgetKnob(\test2);
	}

	tearDown {
		// widget1.remove
	}

	test_cvWidgetLearn {
		var c = CondVar.new;
		var waitDelay = 0.0001;
		var sigDelay = 0.1;

		OSCFunc.cvWidgetLearn(widget1, 0);
		OSCFunc.cvWidgetLearn(widget2, 0, true);
		this.assertEquals(widget1.wmc.oscDisplay.m.value[0], (
			// connectorButVal: 0,
			connectEnabled: false,
			nameField: '/path/to/cmd',
			learn: true,
			connectState: ["waiting...", Color(1.0, 1.0, 1.0), Color(0.5, 0.5, 0.5)],
			index: 1,
			numOscSlots: 1,
			alwaysPositive: 0.1
		), "After calling OSCFunc.cvWidgetLearn widget1.wmc.oscDisplay should have been updated accordingly and the label for the connect button should be \"waiting...\".");
		this.assertEquals(widget1.wmc.oscConnections.m.value, List[nil], "widget1.wmc.oscConnections.m.value should be a List holding nil at index 0 after calling OSCFunc.cvWidgetLearn(widget1, 0).");
		this.assertEquals(widget2.wmc.oscConnections.m.value, List[nil], "widget2.wmc.oscConnections.m.value should be a List holding nil at index 0 after calling OSCFunc.cvWidgetLearn(widget2, 0, true).");
		// NetAddr.localAddr.sendMsg('/test', 6);
		fork {
			waitDelay.wait;
			c.wait({ widget1.wmc.oscConnections.m.value[0].notNil && widget2.wmc.oscConnections.m.value[0].notNil });
			this.assertEquals(widget1.wmc.oscConnections.m.value[0].class, OSCFunc, "After having received an OSC message '/test' from NetAddr.localAddr widget1.wmc.oscConnections.m.value[0] should hold an OSCFunc.");
			this.assertEquals(widget2.wmc.oscConnections.m.value[0].class, OSCFunc, "After having received an OSC message '/test' from NetAddr.localAddr widget2.wmc.oscConnections.m.value[0] should hold an OSCFunc.");
			this.assertEquals(widget1.wmc.oscDisplay.m.value[0], (
				connectState: ["disconnect", Color.white, Color.red],
				connectEnabled: true,
				portField: 57120,
				oscMatching: false,
				numOscSlots: 1,
				index: 1,
				ipField: '127.0.0.1',
				nameField: '/test',
				learn: false
				), "After creating a new OSCFunc widget1.wmc.oscDisplay.m.value[0] should have been set to: (connectState: [\"disconnect\", Color(1.0, 1.0, 1.0), Color(1.0)], connectEnabled: true, portField: 57120, oscMatching: false,
			numOscSlots: 1, index: 1, ipField: 127.0.0.1, nameField: /test, learn: false)");
			this.assertEquals(widget2.wmc.oscDisplay.m.value[0], (
				connectState: ["disconnect", Color.white, Color.red],
				connectEnabled: true,
				portField: 57120,
				oscMatching: true,
				numOscSlots: 1,
				index: 1,
				ipField: '127.0.0.1',
				nameField: '/test',
				learn: false
				), "After creating a new OSCFunc widget2.wmc.oscDisplay.m.value[0] should have been set to: (connectState: [\"disconnect\", Color(1.0, 1.0, 1.0), Color(1.0)], connectEnabled: true, portField: 57120, oscMatching: true,
			numOscSlots: 1, index: 1, ipField: 127.0.0.1, nameField: /test, learn: false)");
			widget1.remove;
			widget2.remove;
		};
		fork {
			NetAddr.localAddr.sendMsg('/test', 6);
			sigDelay.wait;
			c.signalOne;
		}
	}
}

TestExtMIDIFunc : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
		if (MIDIClient.initialized.not) { MIDIClient.init };
	}

	tearDown {
		widget.remove
	}

	test_learnSync {
		widget.wmc.midiConnections.m.value[0] = MIDIFunc.cc.learnSync(widget, 0);
		MIDIIn.doControlAction(12345, 1, 1, 64);
		this.assertEquals([
			widget.wmc.midiConnections.m.value[0].srcID,
			widget.wmc.midiConnections.m.value[0].chan,
			widget.wmc.midiConnections.m.value[0].msgNum,
			widget.wmc.midiConnections.m.value[0].argTemplate
		], [12345, 1, 1, nil], "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction widget.wmc.midiConnections.m.value[0] should return 12345 for srcID, 1 for its chan, 1 for msgNum and nil for argTemplate.");
		this.assertEquals([
			widget.wmc.midiDisplay.m.value[0].src,
			widget.wmc.midiDisplay.m.value[0].chan,
			widget.wmc.midiDisplay.m.value[0].ctrl,
			widget.wmc.midiDisplay.m.value[0].template
		], [12345, 1, 1, nil], "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction widget.wmc.midiDisplay.m.value[0] should return 12345 for src, 1 for its chan, 1 for ctrl and nil for template.");
		widget.midiDisconnect(0);
		widget.wmc.midiConnections.m.value[0] = MIDIFunc.cc.learnSync(widget, 0, true);
		MIDIIn.doControlAction(12345, 1, 1, 64);
		this.assertEquals(widget.wmc.midiConnections.m.value[0].argTemplate, 64, "When calling MIDIFunc.cc.learnSync with arg learnVal set to true the resulting MIDIFunc should return the control value that was sent when learning as its argTemplate.");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].template, 64, "When calling MIDIFunc.cc.learnSync with arg learnVal set to true widget.wmc.midiDisplay.m.value[0] should return the control value that was sent when learning as its template slot.");
	}
}

TestExtFont : UnitTest {
	test_available {
		var font = Font.available("KrixiKraxixxxx", "Bluuuuuuuuuuuub", Font.defaultSansFace);
		this.assertEquals(font, Font.defaultSansFace, "Font.available should return the name of the first available font in the list of given font names.");
	}
}

TestExtOSCCommands : UnitTest {
	test_collectSync {
		var c = CondVar.new;
		var waitDelay = 0.0001;
		var sigDelay = 0.1;

		OSCCommands.collectSync;
		this.assertEquals(CVWidget.wmc.isScanningOsc.m.value, true, "After calling OSCCommands.collectSync(true) CVWidget.wmc.isScanningOsc.m.value should return true.");
		NetAddr.localAddr.sendMsg('/test1', 1, 0.5);
		NetAddr.localAddr.sendMsg('/test2', 34);

		fork {
			waitDelay.wait;
			c.wait({ CVWidget.wmc.oscAddrAndCmds.m.value.isEmpty.not });
			this.assertEquals(CVWidget.wmc.oscAddrAndCmds.m.value, ('127.0.0.1': ('57120': ('/test1': 2, '/test2': 1))), "After sending a message ['/test1', 1, 0.5] and another Message ['/test2', 34] from NetAddr.localAddr CVWidget.wmc.oscAddrAndCmds.m.value should equal ('127.0.0.1': ('57120': ('/test1': 2, '/test2': 1))");
		};

		fork {
			NetAddr.localAddr.sendMsg('/test1', 1, 0.5);
			NetAddr.localAddr.sendMsg('/test2', 34);
			sigDelay.wait;
			c.signalOne;
		}
	}
}

TestExtObject : UnitTest {
	var keys = #[one, two, three];
	var model, controller, res;

	setUp {
		model = Ref(\foo);
		controller = SimpleController(model);
		res = ();
		keys.do { |k|
			controller.put(k, { |changer, what ... moreArgs|
				res[k] = [changer.value, moreArgs[0]];
			})
		}
	}

	tearDown {
		keys.do(controller.removeAt(_))
	}

	test_changedPerformKeys {
		model.changedPerformKeys(keys, \more);
		this.assertEquals(res, (one: [\foo, \more], two: [\foo, \more], three: [\foo, \more]), "Executing changedPerformKeys on a model should trigger all actions added under given keys.")
	}
}