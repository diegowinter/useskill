<script type="text/javascript" src="${pageContext.request.contextPath}/jscripts/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/jscripts/captTester.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/plugin/fancybox/jquery.fancybox.js"></script>
<script type="text/javascript">
(function($){
	$(document).ready(function() {
		$('.fancybox').fancybox();
		
		$.webclientCapt({'url': "${pageContext.request.contextPath}/tarefa/save/fluxo/ideal"});
	});
})(jQuery);
</script>

<div id="inline1" style="width: 400px; display: none;">
	<h3>Roteiro</h3>
	<p>${tarefaDetalhe.roteiro}</p>
</div>
<div id="webclient-topo">
	<div class="direita">
		<a class="btn btn-success concluir" id="concluir12qz3"
			href="${pageContext.request.contextPath}/teste/${testeSession.teste.id}/editar/passo2"><span
			class="icon-ok"></span> <fmt:message key="concluirTarefa" /> </a>
	</div>
	<div class="centro">
		<a class="fancybox btn btn-info" id="roteiro" href="#inline1">Roteiro</a>
	</div>
	<div class="esquerda">
		<a class="bom btn btn-primary">Bom</a> <a class="ruim btn btn-danger">Ruim</a>
		<a class="comentario btn"><fmt:message key="comentario" /> </a>
	</div>
</div>