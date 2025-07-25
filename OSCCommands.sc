OSCCommands {
	classvar <window;
	classvar collectFunc, collectRunning = false, cmds;
	classvar <ipsAndCmds, oscFunc, <tempCollectRunning = false;

	*initClass {
		var localOscFunc;
		var addrWithPort;

		cmds = ();
		collectFunc = { |msg, time, addr, recvPort|
			if (msg[0] !== '/status.reply'){
				cmds.put(msg[0], msg[1..].size);
			}
		};

		ipsAndCmds = ();

		localOscFunc = { |argAddr, argMsg|
			addrWithPort = "%:%".format(argAddr.ip, argAddr.port).asSymbol;

			if (ipsAndCmds.keys.includes(addrWithPort).not and:{
				Server.all.collect(_.addr).includesEqual(argAddr).not
			}) {
				ipsAndCmds.put(addrWithPort, ())
			};
			if (ipsAndCmds.keys.includes(addrWithPort)) {
				ipsAndCmds[addrWithPort].put(argMsg[0], argMsg[1..].size)
			}
		};

		oscFunc = { |msg, time, addr, recvPort| localOscFunc.(addr, msg) }
	}

	*collectIPsAndCmds { |play = true|
		if (play) {
			if (tempCollectRunning == false) {
				thisProcess.addOSCRecvFunc(oscFunc);
			};
			CmdPeriod.add({ this.collectIPsAndCmds(false) });
			tempCollectRunning = true;
		} {
			thisProcess.removeOSCRecvFunc(oscFunc);
			CmdPeriod.remove({ this.collectIPsAndCmds(false) });
			tempCollectRunning = false;
		}
	}

	*collectCmds { |play = true|
		if (play) {
			if (collectRunning == false) {
				thisProcess.addOSCRecvFunc(collectFunc);
				CmdPeriod.add({ this.collectCmds(false) });
				collectRunning = true;
			}
		} {
			thisProcess.removeOSCRecvFunc(collectFunc);
			CmdPeriod.remove({ this.collectCmds(false) });
			collectRunning = false;
		}
	}

	*saveCmdSet { |deviceName|
		var thisDeviceName, allDevices, cmdsPath;

		deviceName ?? {
			Error("Please provide the device- or application-name whose command-names you want to save.").throw;
		};

		this.collectCmds(false);

		thisDeviceName = deviceName.asSymbol;
		cmdsPath = this.filenameSymbol.asString.dirname;
		if (File.exists(cmdsPath+/+"OSCCommands.archive")) {
			allDevices = Object.readArchive(cmdsPath +/+ "OSCCommands.archive");
		} {
			allDevices = ();
		};

		allDevices.put(thisDeviceName, cmds).writeArchive(cmdsPath +/+ "OSCCommands.archive");
		cmds.clear;
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

		this.collectCmds;

		makeField = { |cmds|
			if (fields.keys.size < cmds.size, {
				nextFields = cmds.keys.difference(fields.keys);
				nextFields.do({ |nf|
					fields.put(nf, ());
					fields[nf].cmdName = StaticText(window, Point(390, 20))
						.background_(Color(1.0, 1.0, 1.0, 0.5))
					;
					if (cmds[nf] == 1, {
						fields[nf].cmdName.string_("% (% slot)".format(nf, cmds[nf]));
					}, {
						fields[nf].cmdName.string_("% (% slots)".format(nf, cmds[nf]));
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
				this.collectCmds(false);
				[progressRoutine, collectRoutine].do(_.stop);
				cmds.clear;
			});

			window.view.decorator = flow = FlowLayout(
				window.view.bounds, Point(7, 7), Point(3, 3));

			progress = StaticText(window, Point(flow.indentedRemaining.width, 30))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 20, true))
			;

			flow.nextLine.shift(0, 0);

			progressStates = Pseq(34.collectCmds{ |i| "collecting" + ($.!i).join(' ') }, inf).asStream;
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
						this.collectCmds(false);
						[progressRoutine, collectRoutine].do(_.stop);
						fields.pairsDo({ |k, v|
							if (v.removeBut.value == 1, {
								cmds.removeAt(k);
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
					makeField.(cmds);
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