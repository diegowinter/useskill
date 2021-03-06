package br.ufpi.models;

public enum TipoConvidado {
	EXPERT, USER;

	/**
	 * Obtem o tipo de convidado Foi feito isto pq na view vai ser colocado um
	 * radio buton. Se depois criar novas categorias de Convidados deletar este
	 * metodo
	 * 
	 * @param tipo
	 *            True para USER e False para EXPERT
	 * @return o tipo de convidado
	 */
	public static TipoConvidado parseTipoConvidado(boolean tipo) {
		if (tipo)
			return TipoConvidado.USER;
		return TipoConvidado.EXPERT;

	}
}
