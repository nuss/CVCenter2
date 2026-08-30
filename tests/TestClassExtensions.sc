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
	var knob1, knob2;
	var ms1, ms2;

	setUp {
		knob1 = CVWidgetKnob(\test1);
		knob2 = CVWidgetKnob(\test2);
		ms1 = CVWidgetMS(\ms1);
		ms2 = CVWidgetMS(\ms2);
	}

	tearDown {
		// knob1.remove
	}

	test_cvWidgetLearn {
		var c = CondVar.new;
		var waitDelay = 0.0001;
		var sigDelay = 0.1;

		OSCFunc.cvWidgetLearn(knob1, index: 0);
		OSCFunc.cvWidgetLearn(knob2, index: 0, matching: true);
		OSCFunc.cvWidgetLearn(ms1, 2, 0);
		OSCFunc.cvWidgetLearn(ms2, 2, 0, true);

		this.assertEquals(knob1.wmc.oscDisplay.m.value[0], (
			// connectorButVal: 0,
			connectEnabled: false,
			nameField: '/path/to/cmd',
			learn: true,
			connectState: ["waiting...", Color(1.0, 1.0, 1.0), Color(0.5, 0.5, 0.5)],
			msgSlot: 1,
			numMsgSlots: 1,
			alwaysPositive: 0.1,
			slotToolTip: "CVWidgetKnob '%' holds a single slot - setting not available."
		), "After calling OSCFunc.cvWidgetLearn knob1.wmc.oscDisplay should have been updated accordingly and the label for the connect button should be \"waiting...\".");
		this.assertEquals(knob1.wmc.oscConnections.m.value, List[nil], "knob1.wmc.oscConnections.m.value should be a List holding nil at index 0 after calling OSCFunc.cvWidgetLearn(knob1, 0).");
		this.assertEquals(knob2.wmc.oscConnections.m.value, List[nil], "knob2.wmc.oscConnections.m.value should be a List holding nil at index 0 after calling OSCFunc.cvWidgetLearn(knob2, 0, true).");
		fork {
			waitDelay.wait;
			c.wait({ knob1.wmc.oscConnections.m.value[0].notNil && knob2.wmc.oscConnections.m.value[0].notNil && ms1.wmc.oscConnections.m[2].value[0].notNil && ms2.wmc.oscConnections.m[2].value[0].notNil });
			this.assertEquals(knob1.wmc.oscConnections.m.value[0].class, OSCFunc, "After having received an OSC message '/test' from NetAddr.localAddr knob1.wmc.oscConnections.m.value[0] should hold an OSCFunc.");
			this.assertEquals(knob2.wmc.oscConnections.m.value[0].class, OSCFunc, "After having received an OSC message '/test' from NetAddr.localAddr knob2.wmc.oscConnections.m.value[0] should hold an OSCFunc.");

			this.assertEquals(knob1.wmc.oscDisplay.m.value[0], (
				connectState: ["disconnect", Color.white, Color.red],
				connectEnabled: true,
				portField: 57120,
				oscMatching: false,
				numMsgSlots: 1,
				msgSlot: 1,
				ipField: '127.0.0.1',
				nameField: '/test',
				learn: false,
				alwaysPositive: 0.1,
				slotToolTip: "CVWidgetKnob '%' holds a single slot - setting not available."
			), "After creating a new OSCFunc knob1.wmc.oscDisplay.m.value[0] should have been set to: (connectState: [\"disconnect\", Color(1.0, 1.0, 1.0), Color(1.0)], connectEnabled: true, portField: 57120, oscMatching: false, numMsgSlots: 1, msgSlot: 1, ipField: 127.0.0.1, nameField: /test, learn: false, slotToolTip: \"CVWidgetKnob '%' holds a single slot - setting not available.\")");
			this.assertEquals(knob2.wmc.oscDisplay.m.value[0], (
				connectState: ["disconnect", Color.white, Color.red],
				connectEnabled: true,
				portField: 57120,
				oscMatching: true,
				numMsgSlots: 1,
				msgSlot: 1,
				ipField: '127.0.0.1',
				nameField: '/test',
				learn: false,
				alwaysPositive: 0.1,
				slotToolTip: "CVWidgetKnob '%' holds a single slot - setting not available."
			), "After creating a new OSCFunc knob2.wmc.oscDisplay.m.value[0] should have been set to: (connectState: [\"disconnect\", Color(1.0, 1.0, 1.0), Color(1.0)], connectEnabled: true, portField: 57120, oscMatching: true, numMsgSlots: 1, msgSlot: 1, ipField: 127.0.0.1, nameField: /test, learn: false, slotToolTip: \"CVWidgetKnob '%' holds a single slot - setting not available.\")");

			this.assertEquals(ms1.wmc.oscConnections.m[2].value[0].class, OSCFunc, "After having received an OSC message '/test' from NetAddr.localAddr ms1.wmc.oscConnections.m.value[0] should hold an OSCFunc.");
			this.assertEquals(ms2.wmc.oscConnections.m[2].value[0].class, OSCFunc, "After having received an OSC message '/test' from NetAddr.localAddr ms2.wmc.oscConnections.m.value[0] should hold an OSCFunc.");

			this.assertEquals(ms1.wmc.oscDisplay.m[2].value[0], (
				connectState: ["disconnect", Color.white, Color.red],
				connectEnabled: true,
				portField: 57120,
				oscMatching: false,
				numMsgSlots: 1,
				msgSlot: 1,
				ipField: '127.0.0.1',
				nameField: '/test',
				learn: false,
				alwaysPositive: 0.1,
				slotToolTip: "Select the the CVWidgetMS's '%' slot (widget has % slots)."
			), "After creating a new OSCFunc ms1.wmc.oscDisplay.m.value[0] should have been set to: (connectState: [\"disconnect\", Color(1.0, 1.0, 1.0), Color(1.0)], connectEnabled: true, portField: 57120, oscMatching: false, numMsgSlots: 1, msgSlot: 1, ipField: 127.0.0.1, nameField: /test, learn: false, slotToolTip: \"CVWidgetKnob '%' holds a single slot - setting not available.\")");
			[0, 1, 3, 4].do { |i|
				this.assertEquals(ms1.wmc.oscDisplay.m[i].value[0], (
					connectEnabled: true,
					nameField: '/path/to/cmd',
					learn: true,
					connectState: ["learn", Color(1.0, 1.0), Color(0.0, 0.5)],
					msgSlot: 1,
					numMsgSlots: 1,
					alwaysPositive: 0.1,
					slotToolTip: "Select the the CVWidgetMS's '%' slot (widget has % slots)."
				), "After creating a new OSCFunc ms1.wmc.oscDisplay.m[%].value[0] should remain at its deafult values.".format(i));
			};
			this.assertEquals(ms2.wmc.oscDisplay.m[2].value[0], (
				connectState: ["disconnect", Color.white, Color.red],
				connectEnabled: true,
				portField: 57120,
				oscMatching: true,
				numMsgSlots: 1,
				msgSlot: 1,
				ipField: '127.0.0.1',
				nameField: '/test',
				learn: false,
				alwaysPositive: 0.1,
				slotToolTip: "Select the the CVWidgetMS's '%' slot (widget has % slots)."
			), "After creating a new OSCFunc ms2.wmc.oscDisplay.m.value[0] should have been set to: (connectState: [\"disconnect\", Color(1.0, 1.0, 1.0), Color(1.0)], connectEnabled: true, portField: 57120, oscMatching: true, numMsgSlots: 1, msgSlot: 1, ipField: 127.0.0.1, nameField: /test, learn: false, slotToolTip: \"CVWidgetKnob '%' holds a single slot - setting not available.\")");
			[0, 1, 3, 4].do { |i|
				this.assertEquals(ms2.wmc.oscDisplay.m[i].value[0], (
					connectEnabled: true,
					nameField: '/path/to/cmd',
					learn: true,
					connectState: ["learn", Color(1.0, 1.0), Color(0.0, 0.5)],
					msgSlot: 1,
					numMsgSlots: 1,
					alwaysPositive: 0.1,
					slotToolTip: "Select the the CVWidgetMS's '%' slot (widget has % slots)."
				), "After creating a new OSCFunc ms2.wmc.oscDisplay.m[%].value[0] should remain at its deafult values.".format(i));
			};

			knob1.remove;
			knob2.remove;
			ms1.remove;
			ms2.remove;
		};
		fork {
			NetAddr.localAddr.sendMsg('/test', 6);
			sigDelay.wait;
			c.signalOne;
		}
	}
}

TestExtMIDIFunc : UnitTest {
	var knob, ms;

	setUp {
		knob = CVWidgetKnob(\knob);
		ms = CVWidgetMS(\ms);
		if (MIDIClient.initialized.not) { MIDIClient.init };
	}

	tearDown {
		knob.remove;
		ms.remove;
	}

	test_learnSync {
		knob.wmc.midiConnections.m.value[0] = MIDIFunc.cc.learnSync(knob, index: 0);
		MIDIIn.doControlAction(12345, 1, 1, 64);
		this.assertEquals([
			knob.wmc.midiConnections.m.value[0].srcID,
			knob.wmc.midiConnections.m.value[0].chan,
			knob.wmc.midiConnections.m.value[0].msgNum,
			knob.wmc.midiConnections.m.value[0].argTemplate
		], [12345, 1, 1, nil], "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction knob.wmc.midiConnections.m.value[0] should return 12345 for srcID, 1 for its chan, 1 for msgNum and nil for argTemplate.");
		this.assertEquals([
			knob.wmc.midiDisplay.m.value[0].src,
			knob.wmc.midiDisplay.m.value[0].chan,
			knob.wmc.midiDisplay.m.value[0].ctrl,
			knob.wmc.midiDisplay.m.value[0].template
		], [12345, 1, 1, nil], "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction knob.wmc.midiDisplay.m.value[0] should return 12345 for src, 1 for its chan, 1 for ctrl and nil for template.");
		knob.midiDisconnect(0);
		knob.wmc.midiConnections.m.value[0] = MIDIFunc.cc.learnSync(knob, index: 0, learnVal: true);
		MIDIIn.doControlAction(12345, 1, 1, 64);
		this.assertEquals(knob.wmc.midiConnections.m.value[0].argTemplate, 64, "When calling MIDIFunc.cc.learnSync with arg learnVal set to true the resulting MIDIFunc should return the control value that was sent when learning as its argTemplate.");
		this.assertEquals(knob.wmc.midiDisplay.m.value[0].template, 64, "When calling MIDIFunc.cc.learnSync with arg learnVal set to true knob.wmc.midiDisplay.m.value[0] should return the control value that was sent when learning as its template slot.");
		ms.wmc.midiConnections.m[0].value[0] = MIDIFunc.cc.learnSync(ms, 0, 0);
		MIDIIn.doControlAction(12345, 1, 1, 64);
		this.assertEquals([
			ms.wmc.midiConnections.m[0].value[0].srcID,
			ms.wmc.midiConnections.m[0].value[0].chan,
			ms.wmc.midiConnections.m[0].value[0].msgNum,
			ms.wmc.midiConnections.m[0].value[0].argTemplate
		], [12345, 1, 1, nil], "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction ms.wmc.midiConnections.m[0].value[0] should return 12345 for srcID, 1 for its chan, 1 for msgNum and nil for argTemplate.");
		ms.wmc.midiConnections.m[1..4].value.do { |val, i|
			this.assert(val[0].value.isNil, "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction ms.wmc.midiConnections.m[%].value[0] should hold 'nil'".format(i+1))
		};
		this.assertEquals([
			ms.wmc.midiDisplay.m[0].value[0].src,
			ms.wmc.midiDisplay.m[0].value[0].chan,
			ms.wmc.midiDisplay.m[0].value[0].ctrl,
			ms.wmc.midiDisplay.m[0].value[0].template,
		], [12345, 1, 1, nil], "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction knob.wmc.midiDisplay.m[0].value[0] should return 12345 for src, 1 for its chan, 1 for ctrl and nil for template.");
		ms.wmc.midiDisplay.m[1..4].value.do { |val, i|
			this.assert(val[0].value == (src: 'source...', chan: "chan", ctrl: "ctrl", learn: "L", toolTip: "Click and move hardware slider/knob to connect to", slotToolTip: "Select the the CVWidgetMS's '%' slot (widget has % slots)."), "After calling learnSync on an empty MIDIFunc.cc and executing MIDIIn.doControlAction ms.wmc.midiDisplay.m[%].value[0] should hold an Event (src: 'source...', chan: \"chan\", ctrl: \"ctrl\", learn: \"L\", toolTip: \"Click and move hardware slider/knob to connect to\", slotToolTip: \"Select the the CVWidgetMS's '%' slot (widget has % slots).\")".format(i+1))
		};
		ms.midiDisconnect(0, 0);
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
		var addr = NetAddr("127.0.0.1", 57120);

		OSCCommands.ipsAndCmds.clear;
		OSCCommands.collectSync;
		this.assertEquals(CVWidget.wmc.isScanningOsc.m.value, true, "After calling OSCCommands.collectSync(true) CVWidget.wmc.isScanningOsc.m.value should return true.");
		fork {
			waitDelay.wait;
			c.wait({ CVWidget.wmc.oscAddrAndCmds.m.value.isEmpty.not });
			this.assertEquals(CVWidget.wmc.oscAddrAndCmds.m.value, ('127.0.0.1': ('57120': ('/test1': 2, '/test2': 1))), "After sending a message ['/test1', 1, 0.5] and another Message ['/test2', 34] from NetAddr.localAddr CVWidget.wmc.oscAddrAndCmds.m.value should equal ('127.0.0.1': ('57120': ('/test1': 2, '/test2': 1))");
			OSCCommands.ipsAndCmds.clear;
		};

		fork {
			addr.sendMsg('/test1', 1, 0.5);
			addr.sendMsg('/test2', 34);
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