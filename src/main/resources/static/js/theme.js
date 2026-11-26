(function () {
    const storageKey = 'todo-theme';
    const themes = [
        { id: 'beige', label: 'ベージュ', color: '#F5F0E8' },
        { id: 'pink', label: 'ピンク', color: '#FCEEF3' },
        { id: 'blue', label: '青', color: '#EBF2FA' },
        { id: 'green', label: '緑', color: '#EDF3EB' },
        { id: 'yellow', label: '黄色', color: '#FFF8E3' },
        { id: 'gray', label: '灰色', color: '#ECEEF1' },
        { id: 'dark', label: 'ダーク', color: '#0f172a' }
    ];

    const savedTheme = localStorage.getItem(storageKey) || 'dark';
    const root = document.documentElement;

    function applyTheme(themeId) {
        if (themeId === 'dark') {
            root.removeAttribute('data-theme');
        } else {
            root.dataset.theme = themeId;
        }
        localStorage.setItem(storageKey, themeId);
        document.dispatchEvent(new CustomEvent('themechange', { detail: { theme: themeId } }));
        document.querySelectorAll('.theme-picker__option').forEach((option) => {
            const selected = option.dataset.theme === themeId;
            option.setAttribute('aria-pressed', selected ? 'true' : 'false');
            const check = option.querySelector('.theme-picker__check');
            if (check) {
                check.textContent = selected ? '✓' : '';
            }
        });
    }

    const picker = document.createElement('div');
    picker.className = 'theme-picker';
    picker.innerHTML = '<button class="theme-picker__toggle" type="button" aria-label="カラーテーマを選択" aria-expanded="false">🎨</button>' +
        '<div class="theme-picker__menu" hidden></div>';
    document.body.appendChild(picker);

    const toggle = picker.querySelector('.theme-picker__toggle');
    const menu = picker.querySelector('.theme-picker__menu');
    themes.forEach((theme) => {
        const option = document.createElement('button');
        option.className = 'theme-picker__option';
        option.type = 'button';
        option.dataset.theme = theme.id;
        option.setAttribute('aria-pressed', 'false');
        option.innerHTML = `<span class="theme-picker__swatch" style="background:${theme.color}"></span><span>${theme.label}</span><span class="theme-picker__check"></span>`;
        option.addEventListener('click', () => {
            applyTheme(theme.id);
            menu.hidden = true;
            toggle.setAttribute('aria-expanded', 'false');
        });
        menu.appendChild(option);
    });

    toggle.addEventListener('click', () => {
        menu.hidden = !menu.hidden;
        toggle.setAttribute('aria-expanded', menu.hidden ? 'false' : 'true');
    });

    document.addEventListener('click', (event) => {
        if (!picker.contains(event.target)) {
            menu.hidden = true;
            toggle.setAttribute('aria-expanded', 'false');
        }
    });

    applyTheme(savedTheme);
})();
