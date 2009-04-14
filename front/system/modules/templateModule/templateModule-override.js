/*
 * qWikiOffice Desktop 1.0.0 Alpha
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

// You should probably change the above to fit your module?


Ext.override(QoDesk.TemplateModule, {
	//===========================================================================================
	// 0.0.1 Initial Creation - Date-(YMD): 2008/12/09
	//
	// Module Requirements:
	// - Client Side: ExtJS 2.2, qWikiOffice 1.0 Alpha
	// - Server Side: Tested with PHP4+.
	//
	// - Dependencies: Ext.ux.AboutWindow.js
	//
	// License:
	// - ????
	//
	// NOTE:	This is in the design phase at the moment, please relay any bugs/ideas/suggestions
	//		to the moduleLink URL above.
	//
	// - USAGE:	1) Update the 'templateModule' information above to reflect you new module
	// -		   name.
	// - 		2) .
	// - 		3) .
	// - 		4) .
	//===========================================================================================

	moduleTitle		: 'TemplateModule Module'
	, moduleWindowID	: 'templateModule-win'

	, moduleAuthor		: 'TemplateModule Author'
	, moduleVersion		: '0.0.0'
	, moduleCopyright	: '(c) 2008'

	// Module Launcher Items.
	, moduleIconClass	: 'templateModule-icon'
	, moduleShortcut	: 'templateModule-shortcut-icon'
	, moduleToolTip		: '<b>TemplateModule Module</b><br />  A basic desktop module.'

	, moduleEmail		: 'email@address.com'
	, moduleLink		: 'http://qwikioffice.com/forum/viewtopic.php?f=6&t=169'
	, moduleLogoURL		: 'system/modules/templateModule/icons/logo.png'

	, moduleDescription	: 'This is a basic Module for the qWikiOffice web desktop environment.  It is designed as a template for the creation of a new Module.  See the link above for more information on this module.'

	// Optional About File?
	//, moduleAboutURL	: 'system/modules/templateModule/about/about.txt'

	, moduleAboutMore	: 'Additional About Information: The moduleLink, moduleTitle, moduleVersion, moduleAuthor and moduleDescription are already passed to this function.'


	, moduleHelp		: 'Module Help Information: See the About Link for more info on this module.'
	// Optional Help File?
	//, moduleHelpURL	: 'system/modules/templateModule/about/help.txt'

	, moduleCreditsURL	: 'system/modules/templateModule/about/credits.txt'

	, moduleReadmeURL	: 'system/modules/templateModule/about/readme.txt'

	, moduleLicenseURL	: 'system/modules/templateModule/about/license.txt'
	// Other License Options.
	//, moduleLicenseURL	: '../../license.txt' // ExtJS License.
	//, moduleLicenseURL	: 'LICENSE.txt' // qWikiOffice License.
	//, moduleLicenseURL	: 'system/modules/templateModule/about/license_LGPL_v2.txt'



	// Module Size Options, absolute/relative.
	//, moduleSize		: 'absolute'  // EX: 500 X 600.
	, moduleSize		: 'relative'  // EX: desktop size * (.5=50% X .6=60%).

	// Currently using the 'adjustable' option to size the module.
	, moduleHeight		: '.500'
	, moduleWidth		: '.600'


	
// No longer needed in Ver 1.0 Alpha - now declared in the templateModule.js file?????????
//	, init : function(){
//		this.launcher = {
//			handler : this.createWindow
//			, iconCls: this.moduleIconClass
//			, shortcutIconCls: this.moduleShortcut
//			, scope: this
//			, text: this.moduleTitle
//			, tooltip: this.moduleToolTip
//		}
//	}
	
	, createWindow : function(){

		var desktop = this.app.getDesktop();
		var win = desktop.getWindow(this.moduleWindowID);

		if (!win) {
			// --
			Ext.QuickTips.init();


			// Begin: Module Sizing.
			if (this.moduleSize == 'absolute') {
				var winHeight = parseInt(this.moduleHeight);
				var winWidth = parseInt(this.moduleWidth);
			} else {
				var winHeight = Math.round(desktop.getWinHeight() * this.moduleHeight);
				var winWidth = Math.round(desktop.getWinWidth() * this.moduleWidth);
			}
			moduleAboutHeight = Math.round(winHeight * .9);
			moduleAboutWidth = Math.round(winWidth * .9);
			// End: Module Sizing.


			function serverResponse(title, msg){
			    	var notifyWin = desktop.showNotification();

				notifyWin.setIconClass('x-icon-done');
				notifyWin.setTitle(title);
				notifyWin.setMessage(msg);
				desktop.hideNotification(notifyWin);
			};


			// Use of Ext.ux.AboutWindow Extension.
			var winAbout = new Ext.ux.AboutWindow({
				id: this.moduleId + '-About'

				, title: this.moduleTitle + ' - About'
				, iconCls: 'templateModule-help'

				, modal: false
				, layout: 'fit'

				, height: moduleAboutHeight
				, width: moduleAboutWidth

				//, closeAction: 'hide'

				, plain: true
				, bodyStyle: 'color:#000'

				//, aboutMessageURL: this.moduleAboutURL
				, aboutMessage: '<div id="'+ this.moduleId + '-about"><img src="' + this.moduleLogoURL + '" alt="' + this.moduleTitle + '"></div><div id="' + this.moduleId + '-about-info"><b><a href="' + this.moduleLink + '" target="_blank">' + this.moduleTitle + '</a></b><br />Version: ' + this.moduleVersion + '<br />Author: ' + this.moduleAuthor + ' ' + this.moduleCopyright + ',<br /><br />Description: ' + this.moduleDescription + '<br /><br />' + this.moduleAboutMore + '</div>'


				//, helpMessageURL: this.moduleHelpURL
				, helpMessage: this.moduleHelp

				, moduleCreditsURL: this.moduleCreditsURL
				, moduleReadmeURL: this.moduleReadmeURL
				, moduleLicenseURL: this.moduleLicenseURL

			});

			// Uncomment below if you wish more actions than just winAbout.show();
			//function showAbout() {
			//	winAbout.show();
			//} //showAbout


			win = desktop.createWindow({
				id: this.moduleWindowID

				, title: this.moduleTitle + "( " + winHeight + " X " + winWidth + " )"
				, iconCls: this.moduleIconClass


				, height: winHeight
				, width: winWidth

				, shim:false
				, animCollapse:false
				, constrainHeader:true
				, layout: 'fit'

				, tools:[{
					id:'help'
					, qtip: 'Help'

					, scope: this
					, on:{
						click: function(){
							winAbout.show();
							//showAbout();
						}
					}
				}]

				, html : '<p>templateModule Module.</p>'

				//, items: Grids, Forms, Panels, something?

				, taskbuttonTooltip: this.moduleToolTip
			});
		}
		win.show();
	}
});
