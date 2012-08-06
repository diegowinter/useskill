<%@include file="../leftmenus/default.jsp"%>

<div class="span9 container-right">
	<c:if test="${not empty errors}">
		<div class="alert alert-error">
		<c:forEach items="${errors}" var="error">
        ${error.message}<br />
		</c:forEach>
		</div>
	</c:if>

	<ul class="breadcrumb">
		<li><a href="${pageContext.request.contextPath}/usuario"> <fmt:message
					key="testes.meus" /> </a> <span class="divider">/</span>
		</li>
		<li class="active"><fmt:message key="testes.editar" />
			[<fmt:message key="testes.passo3" />]
		</li>
	</ul>

	<ul class="nav nav-tabs" style="margin: 0 auto; width: 97%">
		<li><a
			href="${pageContext.request.contextPath}/teste/${testeView.teste.id }/editar/passo1"><fmt:message
					key="testes.passo1" /> </a></li>
		<li><a
			href="${pageContext.request.contextPath}/teste/${testeView.teste.id }/editar/passo2"><fmt:message
					key="testes.passo2" /> </a></li>
		<li class="active"><a
			href="${pageContext.request.contextPath}/teste/${testeView.teste.id }/editar/passo3"><fmt:message
					key="testes.passo3" /> </a></li>
		<li><a
			href="${pageContext.request.contextPath}/teste/${testeView.teste.id }/editar/passo4"><fmt:message
					key="testes.passo4" /> </a></li>
	</ul>

	<div class="form-horizontal form-layout">
		<fieldset>
			<legend>
				<span> <fmt:message key="testes.passo3" /> </span>
				<p>
					${testeView.teste.titulo }
				</p>
				<hr />
			</legend>
		</fieldset>

		<fieldset>
			<legend>
				<span><fmt:message key="testes.passo3.listadeusuarios" /> </span>
				<hr />
			</legend>

			<form class="form-horizontal" id="listaUsuarios" 
				action="${pageContext.request.contextPath}/teste/convidar/usuario"
				method="post">
				<table class="table table-striped table-bordered table-condensed">
					<thead>
						<tr>
							<th style="width: 30px"></th>
							<th><fmt:message key="table.nome" /></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${usuariosLivres}" var="usuario">
							<tr>
								<td><input type="checkbox" name="idUsuarios[]"
									value="${usuario.id }"></td>
								<td>${usuario.nome}</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				<input type="hidden" name=idTeste value=${testeView.teste.id }>
					
				<input id="inputUser" type="button" value="<fmt:message key="testes.passo3.usuariosadd"/>" class="btn btn-primary" style="float: right; margin-right: 10px"/>
                <input id="inputTester" type="button" value="<fmt:message key="testes.passo3.testersadd"/>" class="btn btn-primary" style="float: right; margin-right: 5px"/>
                <input id="tipoConvidado" type="hidden" name=tipoConvidado value="true"/>
                
                <input type="submit" style="display:none"/>
			</form>
		</fieldset>
		
		
		<fieldset>
			<legend>
				<span><fmt:message key="testes.passo3.usuariosconvidados" /> </span>
				<hr />
			</legend>

			<form id="removerUsuario" class="form-horizontal"
				action="${pageContext.request.contextPath}/teste/desconvidar/usuario"
				method="post">
				<table class="table table-striped table-bordered table-condensed">
					<thead>
						<tr>
							<th style="width: 30px"></th>
							<th><fmt:message key="table.nome" /></th>
							<th style="width: 70px"><fmt:message key="testes.tipoconvidado" /></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${convidados}" var="convidado">
								<tr>
									<td><input type="checkbox" name="idUsuarios[]"
										value="${convidado.usuario.id }"></td>
									<td>${convidado.usuario.nome}</td>
									<td><fmt:message key="testes.tipo.${convidado.tipoConvidado}" /></td>
								</tr>
							</c:forEach>
					</tbody>
				</table>
				<input type="hidden" name=idTeste value=${testeView.teste.id }>
				<input type="submit"
					value="<fmt:message key="testes.passo3.usuariosremove"/>"
					class="btn btn-danger btn-modal" style="float: right; margin: 0 10px 5px 0" 
					data-toggle="modal" href="#modalMessages" data-acao="Remover"/>
			</form>
		</fieldset>
	</div>
</div>
<script type="text/javascript" src="${pageContext.request.contextPath}/jscripts/libs/jquery.validate.js"></script>
<script>
	(function($){
		$(document).ready(function(){
			var $formListaUsuarios = $('form#listaUsuarios'), $tipoConvidado = $('input#tipoConvidado');
			
			$('#inputUser').click(function(e){
				e.preventDefault();
				$tipoConvidado.val(true);
				$formListaUsuarios.submit();
			});
			
			$('#inputTester').click(function(e){
				e.preventDefault();
				$tipoConvidado.val(false);
				$formListaUsuarios.submit();
			});
		});
	})(jQuery);
</script>