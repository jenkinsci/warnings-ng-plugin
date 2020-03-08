unit RAC.CharacterConsts;

interface

// Contains special Unicode characters to be used as constants for output
// Always save this file as UTF-8
const
  // Ignore extra space behind the characters in the Editor.
  // This is a font/rendering problem. Output looks ok though.
  cCheckMark        = '✓'; // U+2713
  cCheckMarkHeavy   = '✔'; // U+2714
  cCrossMark        = '✕'; // U+2715
  cCrossMarkHeavy   = '✖'; // U+2716
  cCheckBox         = '☐'; // U+2610
  cCheckBoxChecked  = '☒'; // U+2612
  cBlackSquare      = '■'; // U+25A0 "Black" because of paper print!
  cWhiteSquare      = '□'; // U+25A1
  cBlackSquareSmall = '▪'; // U+25AA
  cWhiteSquareSmall = '▫'; // U+25AB
  cBullet           = '•'; // U+2022
  cWhiteBullet      = '◦'; // U+25E6

function Bullet(const ABullet, AString: string): string;

implementation

function Bullet(const ABullet, AString: string): string;
begin
  Result := ABullet + ' ' + AString;
end;

end.
