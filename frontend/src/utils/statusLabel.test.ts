import { describe, expect, it } from 'vitest';
import { statusLabel } from './statusLabel';

describe('statusLabel', () => {
  it('traduz os estados do backend', () => {
    expect(statusLabel('UP')).toBe('Backend conectado');
    expect(statusLabel('DOWN')).toBe('Backend indisponível');
    expect(statusLabel('UNKNOWN')).toBe('Verificando backend');
  });
});
