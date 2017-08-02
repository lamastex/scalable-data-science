{-# LANGUAGE OverloadedStrings #-}
module Main where

-- import Data.Default (def)
-- import qualified Text.Pandoc.Writers.Native as P
-- import System.Environment (getArgs)
import qualified Data.ByteString.Lazy as B
import qualified Data.Text.IO as T
import Data.Monoid ((<>))

import Zeppelin as Z
import Databricks as D
import Notebook as N

import Options.Applicative

type SourceFormat = B.ByteString -> Either String N.Notebook
type TargetFormat = N.Notebook -> B.ByteString

sourceFormat :: Parser SourceFormat
sourceFormat = parseFormat <$> sourceFormat'
  where parseFormat "databricks" x = D.toNotebook <$> D.fromByteString x
        parseFormat "zeppelin"   x = Z.toNotebook <$> Z.fromByteString x
        parseFormat _ _ = error "Unknown source format"
        sourceFormat' = strOption ( long "from"
                                  <> short 'f'
                                  <> metavar "FROM"
                                  <> help "Format to convert from" )

targetFormat :: Parser TargetFormat
targetFormat = parseFormat <$> targetFormat'
  where parseFormat "databricks" = D.toByteString . D.fromNotebook
        parseFormat "zeppelin"   = Z.toByteString . Z.fromNotebook
        parseFormat _ = error "Unknown target format"
        targetFormat' = strOption ( long "to"
                                    <> short 't'
                                    <> metavar "TO"
                                    <> help "Format to convert to" )

data Run = Run { from :: SourceFormat, to :: TargetFormat }

run :: Parser Run
run = Run <$> sourceFormat <*> targetFormat

opts :: ParserInfo Run
opts = info (run <**> helper)
  ( fullDesc
  <> progDesc "Convert between different notebook formats" )

main  :: IO ()
main = do
  (Run from to) <- execParser opts
  result <- from <$> B.getContents
  case result of
    Left e -> error e
    Right n -> B.putStrLn (to n)
