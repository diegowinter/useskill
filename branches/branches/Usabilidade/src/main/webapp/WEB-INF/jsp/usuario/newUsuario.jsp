
<c:if test="${not empty errors}">
    <div class="alert alert-error">
        <c:forEach items="${errors}" var="error">
            ${error.message}<br />
        </c:forEach>
    </div>
</c:if>


<form class="form-horizontal form-layout w600" action="${pageContext.request.contextPath}/usuarios" method="post">
    <c:if test="${not empty usuario.id}">
        <input type="hidden" name="usuario.id" value="${usuario.id}" />
        <input type="hidden" name="_method" value="put" />
    </c:if>
    <fieldset>
        <legend>Adicionar Usuário</legend>
        <div class="control-group">
            <label class="control-label" for="input01"><fmt:message key="nome" />*</label>
            <div class="controls">
                <input type="text" name="usuario.nome" value="${usuario.nome}" class="input-xlarge" id="nome" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input01"><fmt:message key="email" />*</label>
            <div class="controls">
                <input type="text" name="usuario.email" value="${usuario.email}" class="input-xlarge" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input01"><fmt:message key="senha" />*</label>
            <div class="controls">
                <input type="password" name="usuario.senha" id="usuario.senha" class="input-xlarge"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input01"><fmt:message key="confirmasenha" />*</label>
            <div class="controls">
                <input type="password" name="confirmaSenha" value="" class="input-xlarge"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input01"><fmt:message key="telefone" /></label>
            <div class="controls">
                <input type="text" name="usuario.telefones[0]" value="${usuario.telefones[0]}" class="input-xlarge" />
            </div>
        </div>

        <div class="form-actions">
            <input type="submit" value="<fmt:message key="usuario.cadastro"/>" name="enviar" title="Enviar" class="btn btn-primary" style="float: right"/>
        </div>
    </fieldset>
</form>



<script type="text/javascript">
    $(document)
    .ready(
    function() {
   
        $("#editUsuario_Form").validate({
            rules : {
                "usuario.nome" : {
                    required : true
                },
                "usuario.email" : {
                    required : true,
                    email : true
                },
                "usuario.senha" : {
                    required : true,
                    minLength : 6
                },
                "confirmaSenha" : {
                    required : true,
                    minLength : 6
                }
            }
        });
</script>