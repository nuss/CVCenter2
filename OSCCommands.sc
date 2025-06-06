OSCCommands {
	classvar <window;
	classvar collectFunc, collectRunning = false, cmdList;
	classvar <tempIPsAndCmds, oscFunc, <tempCollectRunning = false;

	*initClass {
		var localOscFunc;
		var addrWithPort;

		cmdList = ();
		collectFunc = { |msg, time, addr, recvPort|
			if (msg[0] !== '/status.reply'){
				cmdList.put(msg[0], msg[1..].size);
			}
		};

		tempIPsAndCmds = ();

		localOscFunc = { |argAddr, argMsg|
			addrWithPort = (argAddr.ip.asString++":"++argAddr.port.asString).asSymbol;

			if (tempIPsAndCmds.keys.includes(addrWithPort).not and:{
				Server.all.collect(_.addr).includesEqual(argAddr).not
			}) {
				tempIPsAndCmds.put(addrWithPort, ())
			};
			if (tempIPsAndCmds.keys.includes(addrWithPort)) {
				tempIPsAndCmds[addrWithPort].put(argMsg[0], argMsg[1..].size)
			}
		};

		oscFunc = { |msg, time, addr, recvPort| localOscFunc.(addr, msg) }
	}

	*collectTempIPsAndCmds { |play = true|
		if (play) {
			if (tempCollectRunning == false) {
				thisProcess.addOSCRecvFunc(oscFunc);
			};
			CmdPeriod.add({ this.collectTempIPsAndCmds(false) });
			tempCollectRunning = true;
		} {
			thisProcess.removeOSCRecvFunc(oscFunc);
			CmdPeriod.remove({ this.collectTempIPsAndCmds(false) });
			tempCollectRunning = false;
		}
	}

	*collect { |play = true|
		if (play) {
			if (collectRunning == false) {
				thisProcess.addOSCRecvFunc(collectFunc);
				CmdPeriod.add({ this.collect(false) });
				collectRunning = true;
			}
		} {
			thisProcess.removeOSCRecvFunc(collectFunc);
			CmdPeriod.remove({ this.collect(false) });
			collectRunning = false;
		}
	}

	*saveCmdSet { |deviceName|
		var thisDeviceName, allDevices, cmdsPath;

		deviceName ?? {
			Error("Please provide the device- or application-name whose command-names you want to save.").throw;
		};

		this.collect(false);

		thisDeviceName = deviceName.asSymbol;
		cmdsPath = this.filenameSymbol.asString.dirname;
		if (File.exists(cmdsPath+/+"OSCCommands.archive")) {
			allDevices = Object.readArchive(cmdsPath +/+ "OSCCommands.archive");
		} {
			allDevices = ();
		};

		allDevices.put(thisDeviceName, cmdList).writeArchive(cmdsPath +/+ "OSCCommands.archive");
		cmdList.clear;
	}

	*front {
		var flow, fields = (), deviceNameField, saveBut;
		var progress, progressStates, progressRoutine, collectRoutine, stopFunc;
		var makeField, nextFields;
		var staticTextFont, bigSansFont;
		var staticTextColor = Color(0.2, 0.2, 0.2);
		var textFieldFont, bigMonoFont;
		var textFieldFontColor = Color.black;
		var textFieldBg = Color.white;

		if (Font.respondsTo(\available)) {
			staticTextFont = Font(Font.available("Arial") ? Font.defaultSansFace, 10);
			textFieldFont = Font(Font.available("Courier New") ? Font.defaultMonoFace, 9);
			bigSansFont = Font(Font.available("Arial") ? Font.defaultSansFace, 15);
			bigMonoFont = Font(Font.available("Courier New") ? Font.defaultMonoFace, 15);
		} {
			staticTextFont = Font(Font.defaultSansFace, 10);
			textFieldFont = Font(Font.defaultMonoFace, 9);
			bigSansFont = Font(Font.defaultSansFace, 15);
			bigMonoFont = Font(Font.defaultMonoFace, 15);
		};

		this.collect;

		makeField = { |cmds|
			if (fields.keys.size < cmds.size, {
				nextFields = cmds.keys.difference(fields.keys);
				nextFields.do({ |nf|
					fields.put(nf, ());
					fields[nf].cmdName = StaticText(window, Point(390, 20))
						.background_(Color(1.0, 1.0, 1.0, 0.5))
					;
					if (cmds[nf] == 1, {
						fields[nf].cmdName.string_(nf.asString+"("++cmds[nf]+"slot)");
					}, {
						fields[nf].cmdName.string_(nf.asString+"("++cmds[nf]+"slots)");
					});
					fields[nf].removeBut = Button(window, Point(flow.indentedRemaining.width-20, 20))
						.states_([
							["remove", Color.white, Color.blue],
							["add", Color.white, Color.red],
						])
					;
					flow.nextLine;
				})
			})
		};

		if (window.isNil or:{ window.isClosed }, {
			window = Window("OSC-command-name collector", Rect(
				Window.screenBounds.width/2-250,
				Window.screenBounds.height/2-250,
				500, 500
			), scroll: true);

			window.onClose_({
				this.collect(false);
				[progressRoutine, collectRoutine].do(_.stop);
				cmdList.clear;
			});

			window.view.decorator = flow = FlowLayout(
				window.view.bounds, Point(7, 7), Point(3, 3));

			progress = StaticText(window, Point(flow.indentedRemaining.width, 30))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 20, true))
			;

			flow.nextLine.shift(0, 0);

			progressStates = Pseq(34.collect{ |i| "collecting" + ($.!i).join(' ') }, inf).asStream;
			progressRoutine = fork({
				loop({
					progress.string_(progressStates.next);
					0.5.wait;
				})
			}, AppClock);

			StaticText(window, Point(260, 40)).string_("Collecting command-names will stop as soon as you close this window or save the device's commands. You can only save the command-names after setting a device-name.").font_(staticTextFont);

			deviceNameField = TextField(window, Point(144, 40))
				.font_(Font(Font.available("Courier New") ? Font.defaultMonoFace, 15))
				.string_("< device-name >")
			;

			saveBut = Button(window, Point(flow.indentedRemaining.width-20, 40))
				.states_([["save", Color.white, Color(0.15, 0.5, 0.15)]])
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 15, true))
				.action_({ |b|
					if (deviceNameField.string != "< device-name >" and:{
						deviceNameField.string.size > 0
					}, {
						this.collect(false);
						[progressRoutine, collectRoutine].do(_.stop);
						fields.pairsDo({ |k, v|
							if (v.removeBut.value == 1, {
								cmdList.removeAt(k);
							})
						});
						this.saveCmdSet(deviceNameField.string.asSymbol);
						window.close;
					})
				})
			;

			collectRoutine = fork({
				loop({
					0.1.wait;
					makeField.(cmdList);
				})
			}, AppClock);

			window.view.keyDownAction_({ |view, char, modifiers, unicode, keycode, key|
				if (\KeyDownActions.asClass.notNil) {
					if (keycode == \KeyDownActions.asClass.keyCodes[\return]) { saveBut.doAction };
					if (keycode == \KeyDownActions.asClass.keyCodes[\esc]) { window.close };
				} {
					// "return" key
					if (key == 16777220) { saveBut.doAction };
					// "esc" key
					if (key == 16777216) { window.close };
				}
			})
		});

		window.front;
	}

	*makeWindow {
		this.deprecated(thisMethod, this.class.findMethod(\front));
		^this.front;
	}

	*deviceCmds { |deviceName|
		var thisDeviceName, thisCmds, cmdsPath;

		deviceName !? { thisDeviceName = deviceName.asSymbol };
		if (File.exists(this.filenameSymbol.asString.dirname +/+ "OSCCommands.archive")) {
			cmdsPath = this.filenameSymbol.asString.dirname +/+ "OSCCommands.archive";
		} { ^nil };

		thisCmds = Object.readArchive(cmdsPath);

		if (deviceName.notNil) { ^thisCmds[thisDeviceName] } { ^thisCmds };
	}

	*clearCmdsAt { |deviceName|
		var cmdsPath, cmds;
		if (File.exists(this.filenameSymbol.asString.dirname +/+ "OSCCommands.archive")) {
			cmdsPath = this.filenameSymbol.asString.dirname +/+ "OSCCommands.archive";
			cmds = Object.readArchive(cmdsPath);
			cmds.removeAt(deviceName.asSymbol);
			cmds.writeArchive(cmdsPath);
		}
	}

	*storedDevices {
		var cmdsPath, cmds;
		if (File.exists(this.filenameSymbol.asString.dirname +/+ "OSCCommands.archive")) {
			cmdsPath = this.filenameSymbol.asString.dirname +/+ "OSCCommands.archive";
			cmds = Object.readArchive(cmdsPath);
			^cmds.keys;
		} { ^nil }
	}

}