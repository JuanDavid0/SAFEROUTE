/**
 * Hook para rutas públicas de pedidos (acceso con hash)
 * NO requiere autenticación con JWT
 */

'use client';

import { useEffect, useState } from 'react';

export const usePublicPedido = () => {
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    // Solo marcar como listo después de montar
    setIsReady(true);
  }, []);

  return { isReady };
};
