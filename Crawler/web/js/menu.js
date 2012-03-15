(function ($) {               
    $(document).ready(function(){   
        var selecionado=0;
        var widthSelecionado=0;
        var linksmenu = $('#menu_topo_centro > ul li a');
        var imagem = $('#menu_topo_centro > img.imagem_selected')
        percorre()
        widthSelecionado=centraliza(selecionado)
        anima(widthSelecionado)

        //funcao para percorrer todos os elementos gerando seus dados
        function percorre(){
            linksmenu.each(function(index, value){
                $.data(value,"indice",index)
                $.data(value,"width",$(this).css("width"))
                $.data(value,"paddingE",$(this).css("padding-left"))
                $.data(value,"paddingD",$(this).css("padding-right"))
                $.data(value,"marginE",$(this).css("margin-left"))
                $.data(value,"marginD",$(this).css("margin-right"))
                if($(this).attr("class")=="menu_selected"){
                    selecionado = index;
                }
            });
        }
        //funcao para retornar o centro do elementro
        function centraliza(indice){
            var total=0;
            var esse=0;
            for(var i=0;i<indice;i++){
                total+=parseInt($.data(linksmenu[i],"width"))
                total+=parseInt($.data(linksmenu[i],"paddingE"))
                total+=parseInt($.data(linksmenu[i],"paddingD"))
                total+=parseInt($.data(linksmenu[i],"marginE"))
                total+=parseInt($.data(linksmenu[i],"marginD"))
            }
            //alert(total)
            esse+=parseInt($.data(linksmenu[indice],"width"))/2
            esse+=parseInt($.data(linksmenu[indice],"paddingE"))/2
            esse+=parseInt($.data(linksmenu[indice],"marginE"))/2
            total+=esse
            return total;
        }
        //funcao para animar ate o width passado
        function anima(width){
            imagem.stop().animate({
                left: width
            }, 800, 'easeOutBounce')
        }
                    
        //mouse entrar
        linksmenu.mouseenter(function(){
            anima(centraliza($.data(this,"indice")))
        }).mouseleave(function(){
            anima(widthSelecionado)
        })
        
        linksmenu.bind('click', function(){
            $(this).attr("class","menu_selected")
            percorre()
            widthSelecionado=centraliza(selecionado)
            $("a.menu_selected").removeAttr("class")
        })
    })
})(jQuery);