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
					key="testes.meus" /> </a> <span class="divider">/</span></li>
		<li>
			<a
			href="${pageContext.request.contextPath}/teste/${testeView.teste.id}/editar/passo2">
				<fmt:message key="testes.editar" />
				[<fmt:message key="testes.passo2" />] 
			</a> 
			<span class="divider">/</span>
		</li>

		<li class="active">
			<fmt:message key="pergunta.editar" />
		</li>
	</ul>
	<form class="form-horizontal form-layout"
		action="${pageContext.request.contextPath}/teste/${testeView.teste.id}/editar/passo2/salvar/pergunta"
		method="POST">
		<fieldset>
			<legend>
				<span><fmt:message key="pergunta.editar" /> </span>
				<p>
					${testeView.teste.titulo }
				</p>
				<hr />
			</legend>
			<c:if test="${not empty pergunta.id}">
				<input type="hidden" name="pergunta.id" value="${pergunta.id}" />
				<input type="hidden" name="_method" value="put" />
			</c:if>

			<div class="control-group">
				<label class="control-label" for="input01"><fmt:message
						key="pergunta.titulo" />*</label>
				<div class="controls">
					<input type="text" name="pergunta.titulo"
						value="${pergunta.titulo}" id="titulo" class="span6" />
					<p class="help-block">
						<fmt:message key="pergunta.info.titulo" />
					</p>
				</div>
			</div>

			<div class="control-group">
				<label class="control-label" for="input01"><fmt:message
						key="pergunta.texto" />*</label>
				<div class="controls">
					<textarea rows="10" cols="" name="pergunta.texto" id="texto"
						class="span6">${pergunta.texto}</textarea>
				</div>
			</div>

			<hr />
			<div class="control-group">
				<label class="control-label" for="inlineRadio2"><fmt:message
						key="pergunta.tiporesposta" />
				</label>
				<div class="controls">
					<label class="radio inline"> <input id="respsub"
						type="radio" name="pergunta.tipoRespostaAlternativa"
						id="inlineCheckbox2" value="false"
						<c:if test="${pergunta.tipoRespostaAlternativa==false}" >
               			checked
                    	</c:if> />
						<fmt:message key="pergunta.subjetiva" /> </label> <label
						class="radio inline"> <input id="respobj" type="radio"
						name="pergunta.tipoRespostaAlternativa" id="inlineCheckbox1"
						value="true"
						<c:if test="${pergunta.tipoRespostaAlternativa==true}" >
               				checked
                    	</c:if> />
						<fmt:message key="pergunta.objetiva" /> </label>
				</div>
			</div>
			<hr />

			<div id="grupoalternativas" class="alternativa-group">
				<label class="control-label" for="input01">
					<fmt:message key="pergunta.alternativa" />*
					<br/> 
					<a id="addalternativa" class="btn" href="#">
						<i class="icon-plus"></i> 
					</a> 
				</label>
				
				<c:forEach items="${pergunta.alternativas}" var="alternativa">
					<div class="controls campoalternativa">
						<textarea class="span6 alternativa" rows="2" cols="" name="pergunta.alternativas[].textoAlternativa" style="resize: none;">
							${alternativa.textoAlternativa}
						</textarea>
						<a class="btn btn-primary delalternativa" href="#">
							<i class="icon-trash icon-white"></i>
						</a>
					</div>
				</c:forEach>
				
			</div>
			
			<div class="form-actions">
				<input type="submit" name="pergunta.editar"
					value="<fmt:message key="pergunta.editar"/>" title="<fmt:message key="pergunta.editar"/>"
					class="btn btn-primary" style="float: right" />
			</div>
		</fieldset>
	</form>
</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/jscripts/pergunta.js"></script>