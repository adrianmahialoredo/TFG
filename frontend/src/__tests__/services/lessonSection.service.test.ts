import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/lessonSection.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const section = { id: '1', content: '<p/>' } as any;
const DATA = [{ id: '1' }];
const E = API_ENDPOINTS.LESSON_SECTIONS;

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAll', () => service.getAll(), 'get', [E.BASE], true],
  ['getById', () => service.getById('2'), 'get', [E.BY_ID('2')], true],
  ['getByLesson', () => service.getByLesson('l'), 'get', [E.BY_LESSON('l')], true],
  ['getAllById', () => service.getAllById(['1']), 'post', [E.BY_IDS, ['1']], true],
  ['createLessonSection', () => service.createLessonSection(section), 'post', [E.BASE, section], true],
  ['updateLessonSection', () => service.updateLessonSection('1', section), 'put', [E.BY_ID('1'), section], true],
  ['deleteLessonSection', () => service.deleteLessonSection('1'), 'delete', [E.BY_ID('1')], false],
];

describe('lessonSection.service', () => {
  beforeEach(() => jest.clearAllMocks());

  it.each(cases)('%s llama al endpoint correcto y devuelve los datos', async (_n, call, method, args, returnsData) => {
    api[method].mockResolvedValue({ data: DATA } as any);
    const result = await call();
    expect(api[method]).toHaveBeenCalledWith(...args);
    expect(result).toEqual(returnsData ? DATA : undefined);
  });

  it.each(cases)('%s propaga los errores de la API', async (_n, call, method) => {
    const error = new Error('fallo');
    api[method].mockRejectedValue(error);
    await expect(call()).rejects.toBe(error);
  });
});
