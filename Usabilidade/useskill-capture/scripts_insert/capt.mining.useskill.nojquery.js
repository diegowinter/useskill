/*!
 * Back Button Detection Object V 1.0.1
 * http://www.brookebryan.com/
 *
 * Copyright 2010, Brooke Bryan
 *
 * Date: Thu 27 Jan 2011 13:37 GMT
 */

var bajb_backdetect={Version:'1.0.0',Description:'Back Button Detection',Browser:{IE:!!(window.attachEvent&&!window.opera),Safari:navigator.userAgent.indexOf('Apple')>-1,Opera:!!window.opera},FrameLoaded:0,FrameTry:0,FrameTimeout:null,OnBack:function(){console.log('Back Button Clicked')},BAJBFrame:function(){var BAJBOnBack=document.getElementById('BAJBOnBack');if(bajb_backdetect.FrameLoaded>1){if(bajb_backdetect.FrameLoaded==2){bajb_backdetect.OnBack();history.back()}}bajb_backdetect.FrameLoaded++;if(bajb_backdetect.FrameLoaded==1){if(bajb_backdetect.Browser.IE){bajb_backdetect.SetupFrames()}else{bajb_backdetect.FrameTimeout=setTimeout("bajb_backdetect.SetupFrames();",700)}}},SetupFrames:function(){clearTimeout(bajb_backdetect.FrameTimeout);var BBiFrame=document.getElementById('BAJBOnBack');var checkVar=BBiFrame.src.substr(-11,11);if(bajb_backdetect.FrameLoaded==1&&checkVar!="HistoryLoad"){BBiFrame.src="blank.html?HistoryLoad"}else{if(bajb_backdetect.FrameTry<2&&checkVar!="HistoryLoad"){bajb_backdetect.FrameTry++;bajb_backdetect.FrameTimeout=setTimeout("bajb_backdetect.SetupFrames();",700)}}},SafariHash:'false',Safari:function(){if(bajb_backdetect.SafariHash=='false'){if(window.location.hash=='#b'){bajb_backdetect.SafariHash='true'}else{window.location.hash='#b'}setTimeout("bajb_backdetect.Safari();",100)}else if(bajb_backdetect.SafariHash=='true'){if(window.location.hash==''){bajb_backdetect.SafariHash='back';bajb_backdetect.OnBack();history.back()}else{setTimeout("bajb_backdetect.Safari();",100)}}},Initialise:function(){if(bajb_backdetect.Browser.Safari){setTimeout("bajb_backdetect.Safari();",600)}else{document.write('<iframe src="blank.html" style="display:none;" id="BAJBOnBack" onunload="alert(\'de\')" onload="bajb_backdetect.BAJBFrame();"></iframe>')}}};bajb_backdetect.Initialise();

function useskill_capt_onthefly(obj){
	
	function doAjax(method, url, params, cbSuccess, cbError) {
	    var xmlhttp = new XMLHttpRequest(),
	    	synchronous = false,
	    	cache = false;
	    
	    xmlhttp.onreadystatechange = function() {
	        if (xmlhttp.readyState == XMLHttpRequest.DONE ) {
	           if(xmlhttp.status == 200){
	        	   if(cbSuccess && typeof(cbSuccess) === "function") {
	        		   cbSuccess(xmlhttp.responseText);
	               }
	           }else {
	        	   if(cbError && typeof(cbError) === "function") {
	        		   cbError(xmlhttp.responseText);
	               }
	           }
	        }
	    }

	    url = url + (cache ? (((/\?/).test(url) ? "&" : "?") + (new Date()).getTime()) : '');
	    url = url + (method === 'GET' && params != null ? (((/\?/).test(url) ? "&" : "?") + params) : '');
	    method = method.toUpperCase();
	    
	    xmlhttp.open(method, url, synchronous);
	    xmlhttp.setRequestHeader("Content-type", "application/json;charset=UTF-8");
	    
	    if(method === 'POST' && params != null){
	    	xmlhttp.send(JSON.stringify(params));
	    }else{
	    	xmlhttp.send();
	    }
	}
	
	document.addEventListener("DOMContentLoaded", function(event) {

		console.info("UseSkill Capt Running...");

		if(!obj){
			obj = {};
		}

		//"http://localhost:3000/actions/create";
		//"http://96.126.116.159:3000/actions/create"
		var URL = obj.url ? obj.url : "";
		var CLIENT = obj.client ? obj.client : "";
		var VERSION = obj.version ? obj.version : 0;
		var USERNAME = obj.username ? obj.username : "";
		var ROLE = obj.role ? obj.role : "";
		var SEND_MESSAGES = obj.sendactions ? obj.sendactions : false;
		var DEBUG = obj.debug ? obj.debug : false;
		var CONNECTED_PLUGIN = obj.plugin ? obj.plugin : false;

		var TIME_SUBMIT_SEG = 300;
		var TIME_SUBMIT = TIME_SUBMIT_SEG * 1000;

		var interval = setInterval(function(){
			sendAcoes();
		}, TIME_SUBMIT);

		window.onbeforeunload = function(e) {
			sendAcoes();
		};
		
		function debug(){
			if(DEBUG){
				console.log(arguments);
			}
		}
		
		debug("All Actions in Buffer: ", getAcoes());
		
		// window.onpopstate = function(event) {
		// 	console.log("MUDOU HASH: " + JSON.stringify(event.state));
		// };

		var sending = false;
		
    	function sendAcoes(){
			//return "Are you sure?";
    		if(SEND_MESSAGES){
    			var acoesString = getAcoesString();
				if(acoesString && !sending){
					sending = true;
					doAjax("POST", URL, {acoes:acoesString}, function(result) { 
						debug("Success in Send Actions to Server: ", result);
		            	clearStorage();
		            	sending = false;
		            }, function(data) { 
		            	debug("Error in Send Actions to Server: ", data);
		            	sending = false;
		            });
				}
    		}
    	}
		
		ultimaAcao = null;

		var actionCapt = {
  			CLICK : "click", 
  			FOCUSOUT: "focusout",
  			MOUSEOVER: "mouseover",

  			BROWSER_BACK : "back",
  			BROWSER_FORM_SUBMIT : "form_submit",
  			BROWSER_ONLOAD : "onload",
  			BROWSER_RELOAD : "reload",
  			BROWSER_CLOSE : "close",
  			
  			ROTEIRO : "roteiro",		
  			CONCLUIR : "concluir",		
			COMENTARIO : "comentario",		
			PULAR : "pular"
		};

		function Action(action, time, url, content, tag, tagIndex, id, classe, name, xPath, posX, posY, viewportX, viewportY, useragent) {
			this.sActionType = action;
			this.sTime = time;
			this.sUrl = url;
			this.sContent = String(content).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
			this.sContent = this.sContent.substr(0, 200);
			this.sTag = tag;
			this.sTagIndex = tagIndex;
			this.sId = id;
			this.sClass = classe;
			this.sName = name;
			this.sXPath = xPath;
			this.sPosX = posX;
			this.sPosY = posY;
			this.sViewportX = viewportX;
			this.sViewportY = viewportY;
			this.sUserAgent = useragent;

			this.sRealTime = new Date().getTime();
			this.sTimezoneOffset = new Date().getTimezoneOffset();

			this.sUsername = USERNAME;
			this.sRole = ROLE;
			this.sJhm = getJhmName();
			this.sActionJhm = getActionJhm();
			this.sSectionJhm = getSectionJhm();
			this.sStepJhm = getStepJhm();
			
			this.sClient = CLIENT;
			this.sVersion = VERSION;
		}

		var lastMouseMove = 0, lastMouseX, lastMouseY, mouseOffSet = 5;
		document.addEventListener("click", function(e){ insertNewAcao(e, actionCapt.CLICK);} , false);
		document.addEventListener("focusout", function(e){ insertNewAcao(e, actionCapt.FOCUSOUT);} , false);
		document.addEventListener("mousemove", function(e){
			if(e.timeStamp - lastMouseMove >= 750){
				if(e.pageX <= lastMouseX+mouseOffSet &&
				   e.pageX >= lastMouseX-mouseOffSet &&
				   e.pageY <= lastMouseY+mouseOffSet &&
				   e.pageY >= lastMouseY-mouseOffSet){
					insertNewAcao(e, actionCapt.MOUSEOVER);
				}
			}
			lastMouseMove = e.timeStamp;
			lastMouseX = e.pageX;
			lastMouseY = e.pageY;
		} , false);
		document.addEventListener("submit", function(e){ insertNewAcao(e, actionCapt.BROWSER_FORM_SUBMIT);} , false);
		
		//capturar dados do jheat
		function getJhmName(){
			var $elem = document.getElementsByName('flowName');
			if($elem.length > 0 && $elem.value != ''){
				return $elem.value;
			}else{
				return getRegexValue(/(reportName|flowName|className)\=([^\&|\#]*)/g, true);
			}
		}

		function getSectionJhm(){
			var $elem = document.getElementsByName('sectionName');
			if($elem.length > 0 && $elem.value != ''){
				return $elem.value;
			}else{
				return getRegexValue(/(sectionName)\=([^\&|\#]*)/g, false);
			}
		}

		function getActionJhm(){
			var $elem = document.getElementsByName('action');
			if($elem.length > 0 && $elem.value != ''){
				return $elem.value;
			}else{
				return getRegexValue(/(action)\=([^\&|\#]*)/g, false);
			}		
		}		
				
		function getStepJhm(){		
			var $elem = document.getElementsByName('step');
			if($elem.length > 0 && $elem.value != ''){
				return $elem.value;
			}else{
				return '';
			}
		}

		function getRegexValue(reg, fullSize){
			var matches = reg.exec(window.location.href);
			//{type: matches[1], value: matches[2]} : {type:"", value: ""};
			return matches != null ? (fullSize ? matches[1]+"="+matches[2] : matches[2]) : "";
		}

		//eventos extras, referentes ao carregamento da página e ao botao voltar
		window.onload = function(e){
			insertNewAcao(e, actionCapt.BROWSER_ONLOAD);
		}
		bajb_backdetect.OnBack = function(e){
			insertNewAcao({
				target: document.getElementsByTagName("html"),
				pageX: 0,
				pageY: 0
			}, actionCapt.BROWSER_BACK);
		}
		document.onkeydown = function(event) {
		    if(typeof event != 'undefined'){
		    	//Ctrl+R ou F5
		    	if(event.keyCode == 116 || (event.ctrlKey && event.keyCode == 82)){
		        	insertNewAcao(event, actionCapt.BROWSER_RELOAD);
		    	}
		    	//Ctrl+W ou ALT+F4
			    if((event.ctrlKey && event.keyCode == 87) || (event.altKey && event.keyCode == 114)) { 
					insertNewAcao(event, actionCapt.BROWSER_CLOSE);
				}
				if(event.ctrlKey && event.keyCode == 88){
					sendAcoes();
				}
		    }
		};

		/*	FUNÇÕES EXTRAS	*/
		function insertNewAcao(e, action){
			var target = e.target;
			if(CONNECTED_PLUGIN){		
				action = filterAction(e, target, action);
			}
			if(action){
				var conteudo = getContent(target, action);
				//filtros de captura de dados
				if(
					!(action == actionCapt.FOCUSOUT && conteudo == "")//focusout em campo vazio, não preencheu nada
				){
					var acao = new Action(action, new Date().getTime(), getUrl(), conteudo, target.tagName, getIndexElement(target), target.id, target.className, target.name, createXPathFromElement(e.target), e.pageX, e.pageY, window.innerWidth, window.innerHeight, (get_browser()+" "+get_browser_version()));
					debug("New Action: ", acao);
					//verificar se a acao eh semelhante com a acao passada
					//console.log(acao, acao.sXPath);
					//TODO: se for concluir, verificar se já há outro concluir
					if(ultimaAcao){
						var acoesRapidas = (action == actionCapt.CLICK);
						//se for acao permitida, nao houver acao anterir ou se for mais demorado que 10ms
						if(acoesRapidas || !ultimaAcao.sTime || acao.sActionType != ultimaAcao.sActionType || (acao.sTime && acao.sTime - ultimaAcao.sTime > 10)){
							ultimaAcao = acao;
							addAcao(acao);
						}else{
							ultimaAcao = acao;
							//console.log("Evitou repetição:", ultimaAcao);
						}
					}else{
						//primeira ação
						ultimaAcao = acao;
						addAcao(acao);
					}
					
				}
			}
		}
		function getIndexElement(el){
			var arr = [].slice.call(document.getElementsByTagName(el.tagName));
			return arr.indexOf(el);
		}
		/**
		Receber o conteudo da ação de acordo com o elemento e a ação
		*/
		function getContent(target, action){
			var val = null;
			if(action==actionCapt.COMENTARIO){
				val = document.getElementById('UScomentarioTexto').value;
			}else if(action==actionCapt.CLICK){
				val = target.innerHTML; /* TODO: change to text */
			}else if(action==actionCapt.FOCUSOUT){
				val = target.value;
			}else if(action==actionCapt.MOUSEOVER){
				//receber o title caso seja de um no pai
				var count = 0, parent = target;
				while(val == "" || val == null){
					count++;
					parent = parent.parentNode;
					if(count >= 2 || parent == null){
						val = " ";
					}else{
						val = getContentMouseOver(parent).replace(/^\s+|\s+$/g, "");
					}
				}
			}else if(action==actionCapt.BROWSER_FORM_SUBMIT){
				val = "ACTION=["+target.getAttribute("action")+"], METHOD=["+target.getAttribute("method")+"]";
			}

			if(val){
				return val;
			}else{
				return "";
			}
		}
		
		function getContentMouseOver(target){
			if(target.getAttribute("title")){						
				return target.getAttribute("title");						
			}else if(target.getAttribute("alt")){						
				return target.getAttribute("alt");						
			}else if(target.value){		
				return target.value;		
			}else if(target.innerHtml){		
				return target.innerHTML; /* TODO: change to text */
			}		
			return "";		
		}		
		
		function filterAction(e, el, defaolt){
			var action = false;		
			if(e.type != "focusout"){		
				var isOnUseSkill = getParentsOn(el, document.querySelectorAll('.UseSkill-nocapt'));		
				var id = el.id;		
				var idParent = el.parentNode.id;		
				action = isOnUseSkillDIV(isOnUseSkill, id, idParent);		
			}		
			if(action == true){		
				action = null;		
			}else if(action == false){		
				action = defaolt;		
			}		
			return action;		
		}
		
		function getParents(el) {
		    var parents = [];
		    var p = el.parentNode;
		    while (p !== null) {
		        var o = p;
		        parents.push(o);
		        p = o.parentNode;
		    }
		    return parents; // returns an Array []
		}
		
		function getParentsOn(el, arr) {
			var parents = getParents(el);
			for(var i in arr){
				if(parents.indexOf(arr[i]) != -1){
					return true;
				}
			}
			return false;
		}
		
		function get_browser(){		
		    var ua=navigator.userAgent,tem,M=ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || []; 		
		    if(/trident/i.test(M[1])){		
		        tem=/\brv[ :]+(\d+)/g.exec(ua) || []; 		
		        return 'IE '+(tem[1]||'');		
		        }   		
		    if(M[1]==='Chrome'){		
		        tem=ua.match(/\bOPR\/(\d+)/)		
		        if(tem!=null)   {return 'Opera '+tem[1];}		
		        }   		
		    M=M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];		
		    if((tem=ua.match(/version\/(\d+)/i))!=null) {M.splice(1,1,tem[1]);}		
		    return M[0];		
		}
		
		function get_browser_version(){		
		    var ua=navigator.userAgent,tem,M=ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];                                                                                                                         		
		    if(/trident/i.test(M[1])){		
		        tem=/\brv[ :]+(\d+)/g.exec(ua) || [];		
		        return 'IE '+(tem[1]||'');		
		        }		
		    if(M[1]==='Chrome'){		
		        tem=ua.match(/\bOPR\/(\d+)/)		
		        if(tem!=null)   {return 'Opera '+tem[1];}		
		        }   		
		    M=M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];		
		    if((tem=ua.match(/version\/(\d+)/i))!=null) {M.splice(1,1,tem[1]);}		
		    return M[1];		
		}
		
		/**		
		Funcao para alterar a acao em elementos especificos da UseSkill		
		*/		
		function isOnUseSkillDIV(isOnUseSkill, id, idParent){		
			var action = false;		
			if(isOnUseSkill){		
				action = true;		
				if(id=="USroteiroFull"||idParent=="USroteiroFull"){		
					action = actionCapt.ROTEIRO;		
				}else if(id=="USconcluirTarefa"||idParent=="USconcluirTarefa"){		
					action = actionCapt.CONCLUIR;		
				}else if(id=="UScomentarioEnviar"||idParent=="UScomentarioEnviar"){		
					action = actionCapt.COMENTARIO;		
				}else if(id=="USpularTarefa"||idParent=="USpularTarefa"){		
					action = actionCapt.PULAR;		
				}		
			}		
			return action;		
		}

		function getAcoesString(){
			return localStorage.getItem("useskill_actions");
		}

		function getAcoes(){
			var str = localStorage.getItem("useskill_actions");
			return parseJSON(str);
		}
		
		function printAcoes(){		
			if(CONNECTED_PLUGIN){		
				chrome.extension.sendRequest({useskill: "getAcoes"}, function(dados){		
					debug("Actions: ", dados);		
				});		
			}else{		
				debug(getAcoes());		
			}		
		}
		
		function addAcao(acao){
			if(CONNECTED_PLUGIN){
				var stringAcao = stringfyJSON(acao);
				chrome.extension.sendRequest({useskill: "addAcao", acao: stringAcao});		
			}else{		
				var arr = getAcoes();		
				if(Array.isArray(arr)){		
					arr.push(acao);		
				}else{		
					arr = [acao];		
				}		
				var str = stringfyJSON(arr);		
				debug("Add Action: ", acao);		
				localStorage.setItem("useskill_actions", str);		
			}
		}

		function setItem(name, acoes){
			var x = stringfyJSON(acoes);
			localStorage.setItem(name, x);
		}

		function clearStorage(){
			localStorage.removeItem("useskill_actions");
		}

		function getItem(name){
			var strObj = localStorage.getItem(name);
			return parseJSON(strObj);
		}

		function getUrl(){
			return location.protocol+"//"+location.host+location.pathname;
		}

		function parseJSON(data) {
			return window.JSON && window.JSON.parse ? window.JSON.parse(data) : (new Function("return " + data))(); 
		}

		function stringfyJSON(data){
			return window.JSON && window.JSON.stringify ? window.JSON.stringify(data) : (new Function("return " + data))();
		}

		function createXPathFromElement(elm) {
		    var allNodes = document.getElementsByTagName('*');
		    for (segs = []; elm && elm.nodeType == 1; elm = elm.parentNode){
		        if (elm.hasAttribute('id')) {
		                var uniqueIdCount = 0;
		                for (var n=0;n < allNodes.length;n++) {
		                    if (allNodes[n].hasAttribute('id') && allNodes[n].id == elm.id) uniqueIdCount++;
		                    if (uniqueIdCount > 1) break;
		                };
		                if ( uniqueIdCount == 1) {
		                    segs.unshift('id("' + elm.getAttribute('id') + '")');
		                    return segs.join('/');
		                } else {
		                    segs.unshift(elm.localName.toLowerCase() + '[@id="' + elm.getAttribute('id') + '"]');
		                }
		        } else if (elm.hasAttribute('class')) {
		            segs.unshift(elm.localName.toLowerCase() + '[@class="' + elm.getAttribute('class') + '"]');
		        } else {
		            for (i = 1, sib = elm.previousSibling; sib; sib = sib.previousSibling) {
		                if (sib.localName == elm.localName)  i++; };
		                segs.unshift(elm.localName.toLowerCase() + '[' + i + ']');
		        };
		    };
		    return segs.length ? '/' + segs.join('/') : null;
		}

		function lookupElementByXPath(path) {
		    var evaluator = new XPathEvaluator();
		    var result = evaluator.evaluate(path, document.documentElement, null,XPathResult.FIRST_ORDERED_NODE_TYPE, null);
		    return  result.singleNodeValue;
		}
		
	});
}